let nextCursorCreateTime = null;
let nextCursorId = null;
let loading = false;
let isLoadingMore = false;
let hasMoreData = false;
let currentRecordsCount = 0;
let isMobileDevice = false;

function checkIsMobile() {
  const userAgent = navigator.userAgent;
  const mobileRegex = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i;
  isMobileDevice = mobileRegex.test(userAgent) || window.innerWidth <= 768;
}

function updateDisplay() {
  checkIsMobile();
  const pcTable = document.getElementById('pcTableLayout');
  const mobileGrid = document.getElementById('mobileGrid');
  if (pcTable && mobileGrid) {
    pcTable.style.display = isMobileDevice ? 'none' : 'block';
    mobileGrid.style.display = isMobileDevice ? 'grid' : 'none';
  }
}

function updateLoadMoreButton() {
  const loadMoreTrigger = document.getElementById('loadMoreTrigger');
  const loadingMore = document.getElementById('loadingMore');
  const noMoreData = document.getElementById('noMoreData');

  if (hasMoreData && !isLoadingMore) {
    loadMoreTrigger.style.display = 'block';
    loadingMore.style.display = 'none';
    noMoreData.style.display = 'none';
  } else if (isLoadingMore) {
    loadMoreTrigger.style.display = 'none';
    loadingMore.style.display = 'flex';
    noMoreData.style.display = 'none';
  } else {
    loadMoreTrigger.style.display = 'none';
    loadingMore.style.display = 'none';
    noMoreData.style.display = 'block';
  }
}

function initEventListeners() {
  document.getElementById('wxBtn').onclick = () => location.href = '/care';
  document.getElementById('searchBtn').onclick = () => search(false);
  document.getElementById('resetBtn').onclick = () => {
    document.getElementById('searchName').value = '';
    location.href = '/';
  };
}

function hotSearch(keyword) {
  document.getElementById('searchName').value = keyword.trim();
  search(false);
}

function isImage(url) {
  if (!url) return false;
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
  return imageExtensions.some(ext => url.toLowerCase().includes(ext));
}

function isVideo(url) {
  if (!url) return false;
  const videoExtensions = ['.mp4', '.avi', '.mov', '.wmv', '.flv', '.webm'];
  return videoExtensions.some(ext => url.toLowerCase().includes(ext));
}

function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function formatProductName(name) {
  if (!name || typeof name !== 'string') return '';
  if (name.length <= 60) return name;
  return name.substring(0, 60) + '...';
}

function getAltText(name) {
  if (!name || typeof name !== 'string') return '';
  if (name.length <= 15) return name;
  return name.substring(0, 15);
}

function escapeHtml(v) {
  return (v || '').replace(/[&<>"']/g, s => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[s]));
}

function renderList(records, append) {
  const pcTableBody = document.getElementById('pcTableBody');
  const mobileGrid = document.getElementById('mobileGrid');

  if (!append) {
    pcTableBody.innerHTML = '';
    mobileGrid.innerHTML = '';
  }

  records.forEach(r => {
    const time = r.createTime ? formatDate(r.createTime) : '';
    const imgTag = r.img && (isImage(r.img) || isVideo(r.img))
      ? `<div class="table-image-container"><img src="${r.img}" alt="${escapeHtml(getAltText(r.name))}"></div>`
      : '';

    const pcRow = document.createElement('tr');
    pcRow.setAttribute('data-id', r.id);
    pcRow.className = 'table-row-clickable';
    pcRow.innerHTML = `
      <td>
        <div class="table-title-cell">
          ${imgTag}
          <div class="table-content-container">
            <h3 class="table-product-title" title="${escapeHtml(r.name || '')}">${escapeHtml(formatProductName(r.name))}</h3>
            <p class="table-product-desc">${escapeHtml(r.details || '')}</p>
          </div>
        </div>
      </td>
      <td>
        <div class="table-date-cell">${time}</div>
      </td>
    `;
    pcTableBody.appendChild(pcRow);

    const mobileCard = document.createElement('div');
    mobileCard.className = 'mobile-product-card mobile-card-clickable';
    mobileCard.setAttribute('data-id', r.id);
    const mobileImgTag = r.img && (isImage(r.img) || isVideo(r.img))
      ? `<div class="mobile-card-image"><img src="${r.img}" alt="${escapeHtml(getAltText(r.name))}"></div>`
      : '';
    mobileCard.innerHTML = `
      ${mobileImgTag}
      <div class="mobile-card-content">
        <h3 class="mobile-product-title">${escapeHtml(formatProductName(r.name))}</h3>
        <p class="mobile-product-desc">${escapeHtml(r.details || '')}</p>
        <div class="mobile-card-meta">
          <div class="mobile-user-info">
            <span>👤</span>
            <span>admin</span>
          </div>
          <div class="mobile-date-info">${time}</div>
        </div>
      </div>
    `;
    mobileGrid.appendChild(mobileCard);
  });
}

function loadMoreData() {
  if (hasMoreData && !isLoadingMore) {
    search(true);
  }
}

function search(loadMore) {
  if (loading) return;

  if (!loadMore) {
    const name = document.getElementById('searchName').value.trim();
    if (name.length > 0 && name.length < 2) {
      alert('请输入至少2个字符进行搜索');
      return;
    }
  }

  if (loadMore && (!nextCursorCreateTime || !nextCursorId)) {
    hasMoreData = false;
    updateLoadMoreButton();
    return;
  }

  loading = true;
  if (loadMore) {
    isLoadingMore = true;
    updateLoadMoreButton();
  } else {
    nextCursorCreateTime = null;
    nextCursorId = null;
    hasMoreData = true;
    document.getElementById('searchStatusHint').style.display = 'block';
  }

  const name = document.getElementById('searchName').value.trim();
  let url = '/page/index/data?name=' + encodeURIComponent(name) + '&limit=50';
  if (loadMore) {
    url += '&cursorCreateTime=' + encodeURIComponent(nextCursorCreateTime) + '&cursorId=' + encodeURIComponent(nextCursorId);
  }
  fetch(url).then(r => r.json()).then(res => {
    if (res.code !== 200 || !res.data) return;
    const data = res.data;
    nextCursorCreateTime = data.nextCursorCreateTime || null;
    nextCursorId = data.nextCursorId || null;
    hasMoreData = !!nextCursorCreateTime && (data.records || []).length >= 50;
    document.getElementById('totalCount').textContent = data.total || 0;
    renderList(data.records || [], loadMore);
    updateLoadMoreButton();

    const noSearchResults = document.getElementById('noSearchResults');
    if (!loadMore && (!data.records || data.records.length === 0)) {
      noSearchResults.style.display = 'block';
    } else {
      noSearchResults.style.display = 'none';
    }
  }).finally(() => {
    loading = false;
    if (loadMore) {
      isLoadingMore = false;
      updateLoadMoreButton();
    } else {
      document.getElementById('searchStatusHint').style.display = 'none';
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  });
}

function initPageData() {
  if (window.pageData) {
    nextCursorCreateTime = window.pageData.nextCursorCreateTime || null;
    nextCursorId = window.pageData.nextCursorId || null;
    hasMoreData = !!nextCursorCreateTime;
    currentRecordsCount = (window.pageData.records || []).length;
  }
}

document.addEventListener('click', function(e) {
  const row = e.target.closest('.table-row-clickable');
  if (row) {
    const id = row.getAttribute('data-id');
    if (id) location.href = '/detail/' + id;
    return;
  }
  const card = e.target.closest('.mobile-card-clickable');
  if (card) {
    const id = card.getAttribute('data-id');
    if (id) location.href = '/detail/' + id;
  }
});

initEventListeners();
initPageData();
updateDisplay();
updateLoadMoreButton();