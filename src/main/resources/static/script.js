/* ══════════════════════════════════════════════════════════════
   STOCKSPHERE v2.0 — Frontend Logic
   ══════════════════════════════════════════════════════════════ */

const API_BASE = 'http://localhost:8080';
let priceChart = null;
let lastSymbol = null;

// ── Init ──────────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    checkHealth();
    const input = document.getElementById('symbolInput');
    input.addEventListener('keydown', e => { if (e.key === 'Enter') analyzeStock(); });
    input.addEventListener('input',   e => { e.target.value = e.target.value.toUpperCase(); });
});

// ── Health Check ──────────────────────────────────────────────────────────────
async function checkHealth() {
    const badge = document.getElementById('statusBadge');
    const text  = document.getElementById('statusText');
    try {
        const res = await fetch(`${API_BASE}/health`, { signal: AbortSignal.timeout(3000) });
        if (res.ok) {
            badge.classList.add('online');
            text.textContent = 'BACKEND ONLINE';
        } else throw new Error();
    } catch {
        badge.classList.add('offline');
        text.textContent = 'BACKEND OFFLINE';
    }
}

// ── Quick Analyze ─────────────────────────────────────────────────────────────
function quickAnalyze(symbol) {
    document.getElementById('symbolInput').value = symbol;
    analyzeStock();
}

// ── Main Analyze ──────────────────────────────────────────────────────────────
async function analyzeStock() {
    const input  = document.getElementById('symbolInput');
    const symbol = input.value.trim().toUpperCase();
    if (!symbol) {
        input.focus();
        input.style.borderColor = 'var(--bear)';
        setTimeout(() => input.style.borderColor = '', 1200);
        return;
    }
    lastSymbol = symbol;
    showLoading(symbol);
    try {
        await animateLoadingSteps();
        const res = await fetch(`${API_BASE}/analyze?symbol=${encodeURIComponent(symbol)}`);
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: `HTTP ${res.status}` }));
            throw new Error(err.message || `Server error ${res.status}`);
        }
        const data = await res.json();
        hideLoading();
        renderResults(data);
        document.getElementById('backtestPanel').style.display = 'none';
    } catch (err) {
        hideLoading();
        showError(symbol, err.message);
    }
}

// ── Backtest ──────────────────────────────────────────────────────────────────
async function runBacktest() {
    const input  = document.getElementById('symbolInput');
    const symbol = input.value.trim().toUpperCase();
    if (!symbol) { input.focus(); return; }

    const btn = document.getElementById('backtestBtn');
    btn.disabled = true;
    btn.textContent = '⟳ Running…';

    try {
        const res = await fetch(`${API_BASE}/backtest?symbol=${encodeURIComponent(symbol)}`);
        if (!res.ok) {
            const err = await res.json().catch(() => ({ message: `HTTP ${res.status}` }));
            throw new Error(err.message || 'Backtest failed');
        }
        const data = await res.json();
        renderBacktest(data);
    } catch (err) {
        showError(symbol, 'Backtest error: ' + err.message);
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<span>⟳ BACKTEST</span>';
    }
}

function renderBacktest(data) {
    document.getElementById('backtestPanel').style.display = 'block';
    document.getElementById('backtestStrategy').textContent = data.strategy || '—';
    document.getElementById('btTotal').textContent   = data.totalPredictions ?? '—';
    document.getElementById('btCorrect').textContent = data.correctPredictions ?? '—';
    document.getElementById('btAccuracy').textContent =
        data.accuracyPercentage !== undefined ? data.accuracyPercentage + '%' : '—';
    document.getElementById('btNote').textContent = data.note || '';
    document.getElementById('backtestPanel').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// ── Loading ───────────────────────────────────────────────────────────────────
function showLoading(symbol) {
    document.getElementById('loadingSymbol').textContent = symbol;
    document.getElementById('loadingOverlay').style.display = 'flex';
    document.getElementById('results').style.display    = 'none';
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('errorState').style.display = 'none';
    ['step1','step2','step3','step4'].forEach(id => {
        const el = document.getElementById(id);
        el.classList.remove('active','done');
    });
    const btn = document.getElementById('analyzeBtn');
    btn.classList.add('loading');
    btn.querySelector('.btn-text').textContent = 'ANALYZING';
}

async function animateLoadingSteps() {
    const steps = ['step1','step2','step3','step4'];
    for (let i = 0; i < steps.length; i++) {
        if (i > 0) {
            document.getElementById(steps[i-1]).classList.remove('active');
            document.getElementById(steps[i-1]).classList.add('done');
        }
        document.getElementById(steps[i]).classList.add('active');
        await sleep(350);
    }
}

function hideLoading() {
    document.getElementById('loadingOverlay').style.display = 'none';
    const btn = document.getElementById('analyzeBtn');
    btn.classList.remove('loading');
    btn.querySelector('.btn-text').textContent = 'ANALYZE';
}

// ── Render Results ────────────────────────────────────────────────────────────
function renderResults(data) {
    document.getElementById('results').style.display    = 'block';
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('errorState').style.display = 'none';

    const trend = (data.trend || 'NEUTRAL').toLowerCase();

    // Summary banner
    document.getElementById('summarySymbol').textContent = data.symbol || '—';
    document.getElementById('summaryText').textContent   = data.summary || '—';
    const pill = document.getElementById('trendPill');
    pill.textContent = data.trend || 'NEUTRAL';
    pill.className   = 'trend-pill ' + trend;

    // Price
    document.getElementById('metricPrice').textContent    = fmtPrice(data.currentPrice);
    document.getElementById('metricPriceSub').textContent = data.symbol;

    // SMA 20 + 50
    const sma20str = fmtPrice(data.sma20 || data.sma);
    const sma50str = fmtPrice(data.sma50);
    document.getElementById('metricSMA').textContent = sma20str + ' / ' + sma50str;
    const priceDiff = data.currentPrice && (data.sma20 || data.sma)
        ? (((data.currentPrice - (data.sma20 || data.sma)) / (data.sma20 || data.sma)) * 100).toFixed(2)
        : null;
    const smaPeriodLabel = data.effectiveSmaPeriod || 20;
    document.getElementById('metricSMASub').textContent =
        priceDiff !== null
            ? `${priceDiff > 0 ? '+' : ''}${priceDiff}% vs SMA${smaPeriodLabel}`
            : `vs SMA${smaPeriodLabel}`;

    // Sentiment — show normalised score if available
    const norm = data.sentimentNormalized;
    const dispScore = norm !== undefined
        ? (norm > 0 ? '+' : '') + norm.toFixed(2)
        : (data.sentimentScore !== undefined ? (data.sentimentScore > 0 ? '+' : '') + data.sentimentScore : '—');
    document.getElementById('metricSentimentScore').textContent = dispScore;
    const sentTag = document.getElementById('metricSentimentTag');
    sentTag.textContent = (data.sentiment || 'neutral').toUpperCase();
    sentTag.className   = 'metric-sub sentiment-tag ' + (data.sentiment || 'neutral');

    // Confidence
    document.getElementById('metricConfidence').textContent =
        data.confidence !== undefined ? data.confidence + '%' : '—';
    document.getElementById('metricTrend').textContent = data.trend || '—';

    renderChart(data);
    renderSignals(data);
    renderSentiment(data);
    document.getElementById('results').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

// ── Chart (Price + Rolling SMA20 + Rolling SMA50) ─────────────────────────────
function renderChart(data) {
    const ctx = document.getElementById('priceChart').getContext('2d');
    if (priceChart) { priceChart.destroy(); priceChart = null; }

    const prices = data.prices || [];
    const dates  = data.dates  || prices.map((_, i) => `D${i+1}`);

    // Use rolling SMA history arrays from the backend.
    // These arrays are aligned 1:1 with prices — null entries where history is insufficient.
    // Fall back to flat scalar lines only if history arrays are absent (backward compat).
    const sma20Data = (data.sma20History && data.sma20History.length === prices.length)
        ? data.sma20History
        : prices.map(() => data.sma20 || data.sma || null);

    const sma50Data = (data.sma50History && data.sma50History.length === prices.length)
        ? data.sma50History
        : prices.map(() => data.sma50 || null);

    const labelStep = Math.max(1, Math.floor(dates.length / 8));
    const labels    = dates.map((d, i) => (i % labelStep === 0) ? formatDate(d) : '');

    const datasets = [
        {
            label: 'Price',
            data: prices,
            borderColor: '#00c896',
            backgroundColor: 'rgba(0,200,150,0.06)',
            borderWidth: 2,
            pointRadius: 0,
            pointHoverRadius: 4,
            fill: true,
            tension: 0.3,
            spanGaps: false,
            order: 1,
        },
        {
            label: `SMA (${data.effectiveSmaPeriod || 20})`,
            data: sma20Data,
            borderColor: '#f5c842',
            borderWidth: 1.5,
            borderDash: [6, 4],
            pointRadius: 0,
            fill: false,
            tension: 0,
            spanGaps: false,
            order: 2,
        }
    ];

    if (sma50Data.some(v => v !== null)) {
        datasets.push({
            label: `SMA (${data.sma50 ? 50 : '—'})`,
            data: sma50Data,
            borderColor: '#a78bfa',
            borderWidth: 1.5,
            borderDash: [3, 6],
            pointRadius: 0,
            fill: false,
            tension: 0,
            spanGaps: false,
            order: 3,
        });
    }

    priceChart = new Chart(ctx, {
        type: 'line',
        data: { labels, datasets },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: '#0e1318',
                    borderColor: '#1e2a35',
                    borderWidth: 1,
                    titleColor: '#5a7085',
                    bodyColor: '#d4dde8',
                    titleFont: { family: "'Space Mono', monospace", size: 10 },
                    bodyFont: { family: "'Space Mono', monospace", size: 12 },
                    callbacks: {
                        label: ctx => ctx.parsed.y !== null
                            ? ` ${ctx.dataset.label}: ${fmtPrice(ctx.parsed.y)}`
                            : null,
                        title: items => dates[items[0].dataIndex] || ''
                    }
                }
            },
            scales: {
                x: {
                    grid:   { color: 'rgba(30,42,53,0.5)' },
                    ticks:  { color: '#3a5060', font: { family: "'Space Mono', monospace", size: 9 }, maxRotation: 0 },
                    border: { color: '#1e2a35' }
                },
                y: {
                    grid:   { color: 'rgba(30,42,53,0.5)' },
                    ticks:  { color: '#3a5060', font: { family: "'Space Mono', monospace", size: 9 }, callback: v => fmtPrice(v) },
                    border: { color: '#1e2a35' }
                }
            }
        }
    });
}

// ── Signal Grid ───────────────────────────────────────────────────────────────
function renderSignals(data) {
    const votes = data.signalVotes || {};
    const grid  = document.getElementById('signalGrid');
    grid.innerHTML = '';

    const icons = { SMA: '⟳', Crossover: '✕', Momentum: '↗', Volume: '⬡', Sentiment: '◎' };

    Object.entries(votes).forEach(([name, vote]) => {
        const card = document.createElement('div');
        card.className = 'signal-card ' + (vote || 'neutral');
        card.innerHTML = `
            <div class="signal-name">${(icons[name] || '●')} ${escapeHtml(name.toUpperCase())}</div>
            <div class="signal-value ${escapeHtml(vote || 'neutral')}">
                <span class="signal-icon">${signalIcon(vote)}</span>${escapeHtml((vote || 'neutral').toUpperCase())}
            </div>`;
        grid.appendChild(card);
    });

    // Confidence bar
    const pct    = data.confidence || 0;
    const isBear = data.trend === 'BEARISH';
    document.getElementById('confPct').textContent = pct + '%';
    const fill = document.getElementById('confBar');
    fill.style.width = '0%';
    fill.className = 'confidence-bar-fill' + (isBear ? ' bearish' : '');
    setTimeout(() => { fill.style.width = pct + '%'; }, 100);
}

function signalIcon(vote) {
    if (vote === 'bullish') return '▲';
    if (vote === 'bearish') return '▼';
    return '◆';
}

// ── Sentiment + News ──────────────────────────────────────────────────────────
function renderSentiment(data) {
    const sentiment = data.sentiment || 'neutral';
    const norm      = data.sentimentNormalized ?? 0;

    const pill = document.getElementById('sentimentPill');
    pill.textContent = sentiment.toUpperCase();
    pill.className = 'sentiment-pill ' + sentiment;

    // Map normalised score [-1, +1] → [0%, 100%]
    const pct = ((norm + 1) / 2) * 100;
    document.getElementById('sentimentNeedle').style.left = pct + '%';
    document.getElementById('sentimentNormLabel').textContent =
        'Score: ' + (norm > 0 ? '+' : '') + norm.toFixed(2) + ' (normalised)';

    // News list
    const articles = data.articles || [];
    const list = document.getElementById('newsList');
    list.innerHTML = '';

    if (articles.length === 0) {
        list.innerHTML = '<div style="color:var(--text-muted);font-size:0.78rem;padding:10px 0">No news articles found.</div>';
        return;
    }

    articles.forEach(article => {
        const item = document.createElement('div');
        item.className = 'news-item';
        // noopener,noreferrer prevents the opened page from accessing window.opener
        if (article.url) {
            item.onclick = () => window.open(article.url, '_blank', 'noopener,noreferrer');
        }
        item.innerHTML = `
            <div class="news-title">${escapeHtml(article.title || 'No title')}</div>
            <div class="news-meta">
                <span>${escapeHtml(article.source || '')}</span>
                <span>${formatDatetime(article.publishedAt || '')}</span>
            </div>`;
        list.appendChild(item);
    });
}

// ── Error State ───────────────────────────────────────────────────────────────
function showError(symbol, message) {
    document.getElementById('results').style.display    = 'none';
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('errorState').style.display = 'block';
    document.getElementById('errorTitle').textContent = `Analysis failed for ${escapeHtml(symbol)}`;
    document.getElementById('errorSub').textContent   =
        message || 'Could not connect to StockSphere backend. Ensure the server is running on localhost:8080.';
}

function retryAnalysis() {
    if (lastSymbol) {
        document.getElementById('symbolInput').value = lastSymbol;
        analyzeStock();
    } else {
        document.getElementById('errorState').style.display  = 'none';
        document.getElementById('emptyState').style.display  = 'block';
    }
}

// ── Utilities ─────────────────────────────────────────────────────────────────
function fmtPrice(val) {
    if (val === null || val === undefined) return '—';
    const num = parseFloat(val);
    if (isNaN(num)) return '—';
    if (num >= 100) return num.toLocaleString('en-IN', { maximumFractionDigits: 2 });
    return num.toFixed(2);
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    try {
        return new Date(dateStr).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' });
    } catch { return dateStr; }
}

function formatDatetime(dateStr) {
    if (!dateStr) return '';
    try {
        return new Date(dateStr.replace(' ', 'T')).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' });
    } catch { return dateStr; }
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function sleep(ms) { return new Promise(r => setTimeout(r, ms)); }
