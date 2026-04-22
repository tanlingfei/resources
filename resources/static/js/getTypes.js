function isQuark() {
  /*return true;*/
  const ua = navigator.userAgent.toLowerCase();
  return ua.includes('quark') || ua.includes('quarkbrowser');
}

function truncateName(name, maxLength) {
  if (name && name.length > maxLength) {
    return name.substring(0, maxLength) + '...';
  }
  return name;
}

function getDeviceId() {
  if (isQuark()) {
    let deviceId = localStorage.getItem('quark_device_id');
    if (!deviceId) {
      deviceId = 'quark_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
      localStorage.setItem('quark_device_id', deviceId);
    }
    return deviceId;
  }
  return null;
}

async function fetchClipboard(deviceId) {
  try {
    const response = await fetch('/getClipboard?deviceId=' + encodeURIComponent(deviceId));
    const result = await response.json();
    if (result.code === 200 && result.data) {
      return result.data;
    }
    return null;
  } catch (error) {
    console.log('获取剪贴板记录失败:', error);
    return null;
  }
}

async function fetchDownloadLinks(text) {
  try {
    const formData = new FormData();
    formData.append('text', text);
    const response = await fetch('/business/commodityCommodityinfos/getTypes', {
      method: 'POST',
      body: formData
    });
    const result = await response.json();
    if (result.code === 200) {
      return result.data;
    }
    return null;
  } catch (error) {
    console.log('获取下载链接失败:', error);
    return null;
  }
}

function renderResult(data) {
  const resultSection = document.getElementById('resultSection');
  if (!data || !data.list || data.list.length === 0) {
    resultSection.innerHTML = `
      <div class="result-header">📥 素材信息</div>
      <div class="link-item">
        <div class="link-name">《Java从入门到实战》:</div>
        <div class="link-desc">从基础到项目实战，系统掌握Java开发，附赠配套视频和源码，轻松上手</div>
        <a href="https://pan.quark.cn/s/74ee031b0f09" target="_blank" class="link-url">https://pan.quark.cn/s/74ee031b0f09</a>
      </div>
    `;
    return;
  }

  let html = '';
  if (data.name) {
    html += '<div class="title-section"><h3 class="material-title">' + truncateName(data.name, 30) + '</h3></div>';
  }
  html += '<div class="result-header">📥 下载链接</div>';

  data.list.forEach(function(link) {
    html += '<div class="link-item">';
    html += '<div class="link-name">' + (link.name || '') + ' :</div>';
    if (link.url) {
      html += '<a href="' + link.url + '" target="_blank" class="link-url">' + link.url + '</a>';
    }
    html += '</div>';
  });

  resultSection.innerHTML = html;
}

async function loadData() {
  const deviceId = getDeviceId();
  if (!deviceId) {
    document.getElementById('contentContainer').innerHTML = `
      <div class="not-quark-section">
        <div class="not-quark-icon">📱</div>
        <div class="not-quark-title">请在夸克App中打开</div>
        <div class="not-quark-desc">
          此页面只能在App中访问，请使用App打开获取资源。
        </div>
      </div>
    `;
    return;
  }

  const clipboardContent = await fetchClipboard(deviceId);
  if (!clipboardContent) {
    renderResult(null);
    return;
  }

  const data = await fetchDownloadLinks(clipboardContent);
  renderResult(data);
}

window.onload = function() {
  if (!isQuark()) {
    const contentContainer = document.getElementById('contentContainer');
    contentContainer.innerHTML = `
      <div class="not-quark-section">
        <div class="not-quark-icon">📱</div>
        <div class="not-quark-title">请在夸克App中打开</div>
        <div class="not-quark-desc">
          此页面只能在夸克App中访问，请使用夸克App打开获取资源。
        </div>
      </div>
    `;
  } else {
    loadData();
  }
};
