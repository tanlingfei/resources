function getShareLink() {
  return window.shareLink || document.getElementById('shareLinkData')?.value || '';
}

function isMobile() {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

function isWechat() {
  return navigator.userAgent.toLowerCase().indexOf('micromessenger') !== -1;
}

function isQuark() {
  return navigator.userAgent.toLowerCase().indexOf('quark') !== -1;
}

function copyText(txt) {
  return new Promise((resolve) => {
    if (!txt) {
      resolve(false);
      return;
    }
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(txt).then(() => resolve(true)).catch(() => {
        fallbackCopy(txt);
        resolve(true);
      });
    } else {
      fallbackCopy(txt);
      resolve(true);
    }
  });
}

function fallbackCopy(txt) {
  const i = document.createElement('input');
  i.value = txt;
  document.body.appendChild(i);
  i.select();
  document.execCommand('copy');
  document.body.removeChild(i);
}

async function saveClipboard(content, deviceId) {
  try {
    await fetch('/saveClipboard', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: `content=${encodeURIComponent(content)}&deviceId=${encodeURIComponent(deviceId)}`
    });
  } catch (error) {
    console.log('保存剪贴板记录失败:', error);
  }
}

async function getQuarkResource() {
  if (!isMobile()) {
    alert('此功能仅支持手机端操作，请在手机上打开此页面');
    return;
  }
  const shareLink = getShareLink();
  if (!shareLink) {
    alert('暂无分享链接');
    return;
  }
  const copyContent = shareLink.split('#')[0];
  await copyText(copyContent);



  await saveClipboard(shareLink, getDeviceId());

  if (isWechat()) {
    alert('链接已复制，由于微信环境限制，请手动打开夸克网盘APP查看资源。');
  } else if (!isQuark()) {
    openQuark();
  } else {
    openQuark();
  }
}

async function downloadQuark() {
  if (!isMobile()) {
    alert('此功能仅支持手机端操作，请在手机上打开此页面');
    return;
  }
  const shareLink = getShareLink();
  if (!shareLink) {
    alert('暂无分享链接');
    return;
  }
  if (isQuark()) {
    alert('您已在夸克环境中，无需下载夸克APP');
    return;
  }
  await copyText(shareLink.split("#")[0]);


  await saveClipboard(shareLink, getDeviceId());
  location.href = 'https://pan.quark.cn/';
}

function generateUniqueId() {
  return 'quark_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

function getDeviceId() {
  if (isQuark()) {
    let deviceId = localStorage.getItem('quark_device_id');
    if (!deviceId) {
      deviceId = generateUniqueId();
      localStorage.setItem('quark_device_id', deviceId);
    }
    return deviceId;
  } else {
    return '13621215240';
  }
}

function openQuark() {
  if (isMobile()) {
    const quarkScheme = 'quark://';
    const hiddenIframe = document.createElement('iframe');
    hiddenIframe.style.display = 'none';
    hiddenIframe.src = quarkScheme;
    document.body.appendChild(hiddenIframe);

    let timer = null;
    let hasOpenedApp = false;

    const handleVisibilityChange = () => {
      if (document.hidden) {
        hasOpenedApp = true;
        clearTimeout(timer);
      }
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    timer = setTimeout(() => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
      document.body.removeChild(hiddenIframe);

      if (!hasOpenedApp) {
        if (isQuark()) {
          alert('请手动允许查看资源');
          return;
        }
        if (confirm('检测到您可能未安装夸克网盘或未允许打开。您可以：\n\n• 点击「确定」打开夸克官网下载夸克\n• 点击「取消」关闭此提示，手动打开夸克网盘')) {
          window.location.href = 'https://pan.quark.cn/';
        }
      }
    }, 2000);
  } else {
    window.location.href = 'https://pan.quark.cn/';
  }
}



