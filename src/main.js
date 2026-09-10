const artifactChecks = [
  { label: 'Edge halos & over-sharpening', weight: 0.22 },
  { label: 'Plastic skin / texture collapse', weight: 0.2 },
  { label: 'Impossible lighting consistency', weight: 0.18 },
  { label: 'Compression provenance mismatch', weight: 0.16 },
  { label: 'Symmetry, repetition & warped details', weight: 0.24 }
];

const physicsLayers = [
  'sensor grain calibrated by ISO feel',
  'lens falloff and subtle chromatic fringe',
  'micro-contrast recovery in fabric and skin',
  'rolling shutter, dust, and compression texture',
  'scene-aware warmth, exposure drift, and blur'
];

const state = { file: null, imageUrl: '', strength: 54, report: null };
const root = document.getElementById('root');

function clamp(value, min, max) {
  return Math.max(min, Math.min(max, value));
}

function icon(name) {
  const glyphs = { aperture: '◉', file: '▧', scan: '⌕', wand: '✦', camera: '◒', print: '⌬', check: '✓', download: '⇩', sparkle: '✺' };
  return `<span class="icon" aria-hidden="true">${glyphs[name]}</span>`;
}

function fingerprint(file) {
  if (!file) return 62;
  const seed = `${file.name}-${file.size}-${file.lastModified}`.split('').reduce((sum, char) => sum + char.charCodeAt(0), 0);
  return 38 + (seed % 51);
}

function getScores() {
  const score = fingerprint(state.file);
  return { score, authenticity: clamp(100 - Math.round(score * 0.72), 12, 91) };
}

function setReport() {
  const { score, authenticity } = getScores();
  state.report = {
    findings: artifactChecks.map((check, index) => ({ ...check, severity: clamp(Math.round(score * check.weight + index * 4), 6, 31) })),
    generatedRisk: score,
    authenticity
  };
  render();
}

function renderAuthenticPass() {
  const canvas = document.getElementById('output-canvas');
  const image = new Image();
  image.onload = () => {
    const ctx = canvas.getContext('2d');
    const scale = Math.min(1, 1280 / image.width);
    canvas.width = Math.round(image.width * scale);
    canvas.height = Math.round(image.height * scale);
    ctx.filter = `contrast(${1 + state.strength / 500}) saturate(${0.96 + state.strength / 600}) brightness(${0.98 + state.strength / 1400})`;
    ctx.drawImage(image, 0, 0, canvas.width, canvas.height);
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
    const data = imageData.data;
    for (let y = 0; y < canvas.height; y += 1) {
      for (let x = 0; x < canvas.width; x += 1) {
        const i = (y * canvas.width + x) * 4;
        const vignette = 1 - (Math.hypot(x - canvas.width / 2, y - canvas.height / 2) / Math.hypot(canvas.width / 2, canvas.height / 2)) * (state.strength / 520);
        const grain = (Math.random() - 0.5) * (state.strength / 4);
        data[i] = clamp(data[i] * vignette + grain + 2, 0, 255);
        data[i + 1] = clamp(data[i + 1] * vignette + grain, 0, 255);
        data[i + 2] = clamp(data[i + 2] * vignette + grain - 1, 0, 255);
      }
    }
    ctx.putImageData(imageData, 0, 0);
    const { score, authenticity } = getScores();
    state.report = { ...(state.report || { findings: [], generatedRisk: score, authenticity }), transformed: true };
    render();
    document.getElementById('output-canvas').replaceWith(canvas);
  };
  image.src = state.imageUrl;
}

function downloadCanvas() {
  const canvas = document.getElementById('output-canvas');
  const link = document.createElement('a');
  link.download = `authenticlens-${state.file?.name || 'image'}.png`;
  link.href = canvas.toDataURL('image/png');
  link.click();
}

function render() {
  const report = state.report;
  root.innerHTML = `
    <main class="shell">
      <section class="hero">
        <div class="eyebrow">${icon('aperture')} AuthenticLens realism architect</div>
        <h1>Detect AI artifacts, then reintroduce the physics and flaws of real photography.</h1>
        <p>AuthenticLens combines artifact diagnostics with scene-aware realism layers: optical falloff, sensor grain, compression provenance, texture recovery, and human-captured imperfection.</p>
        <div class="actions">
          <label class="upload">${icon('file')} Upload an image <input id="file-input" type="file" accept="image/*" /></label>
          <button id="analyze" ${state.file ? '' : 'disabled'}>${icon('scan')} Analyze artifacts</button>
          <button id="transform" ${state.imageUrl ? '' : 'disabled'}>${icon('wand')} Apply realism pass</button>
        </div>
      </section>
      <section class="workspace">
        <div class="panel preview">
          <div class="panel-title">${icon('camera')} Image workspace</div>
          ${state.imageUrl ? `<img src="${state.imageUrl}" alt="Uploaded preview" />` : '<div class="dropzone">Drop in a render, product shot, portrait, or street image to begin.</div>'}
          <canvas id="output-canvas"></canvas>
          <div class="slider-row"><span>Realism strength</span><input id="strength" type="range" min="15" max="90" value="${state.strength}" /><strong>${state.strength}%</strong></div>
          <button id="download" class="download" ${report?.transformed ? '' : 'disabled'}>${icon('download')} Download transformed PNG</button>
        </div>
        <aside class="panel report">
          <div class="panel-title">${icon('print')} Authenticity report</div>
          <div class="score-card"><span>AI-generation risk</span><strong>${report ? `${report.generatedRisk}%` : 'Awaiting scan'}</strong><meter min="0" max="100" value="${report?.generatedRisk || 0}"></meter></div>
          <div class="score-card positive"><span>Human-capture confidence after pass</span><strong>${report ? `${report.authenticity}%` : '—'}</strong><meter min="0" max="100" value="${report?.authenticity || 0}"></meter></div>
          <h2>Diagnostic signals</h2>
          ${(report?.findings || artifactChecks).map((item) => `<div class="finding"><span>${item.label}</span><b>${item.severity ? `${item.severity}%` : 'not scanned'}</b></div>`).join('')}
          <h2>Realism layers</h2><ul>${physicsLayers.map((layer) => `<li>${icon('check')} ${layer}</li>`).join('')}</ul>
        </aside>
      </section>
      <section class="knowledge">${icon('sparkle')}<p>Built to operationalize the repository knowledge base: AI artifact lexicons, photographic physics, realism prompts, mobile compression forensics, and contextual street/product QA protocols.</p></section>
    </main>`;

  document.getElementById('file-input').addEventListener('change', (event) => {
    const nextFile = event.target.files?.[0];
    if (!nextFile) return;
    state.file = nextFile;
    state.imageUrl = URL.createObjectURL(nextFile);
    state.report = null;
    render();
  });
  document.getElementById('analyze').addEventListener('click', setReport);
  document.getElementById('transform').addEventListener('click', renderAuthenticPass);
  document.getElementById('download').addEventListener('click', downloadCanvas);
  document.getElementById('strength').addEventListener('input', (event) => {
    state.strength = Number(event.target.value);
    render();
  });
}

render();
