const API_BASE = '/api';
let currentPage = 0;
let currentCategory = 'Home';
let currentSearch = '';
let isLoading = false;
let userToken = localStorage.getItem('globalpulse_token');
let refreshToken = localStorage.getItem('globalpulse_refresh_token');
let username = localStorage.getItem('globalpulse_username');
let searchHistory = JSON.parse(localStorage.getItem('globalpulse_search_history')) || [];
const apiCache = new Map();
let seenNewsTitles = new Set();

document.addEventListener('DOMContentLoaded', () => {
    initTheme();
    initAuth();
    initSearchHistory();
    loadTrending();
    loadLatest();
    loadBreaking();
    setupEventListeners();
});

function initAuth() {
    const authSection = document.getElementById('authSection');
    if (userToken && username) {
        authSection.innerHTML = `
            <div class="dropdown">
                <button class="btn btn-outline-secondary dropdown-toggle" type="button" data-bs-toggle="dropdown">
                    <i class="fas fa-user-circle me-1"></i>${username}
                </button>
                <ul class="dropdown-menu dropdown-menu-end">
                    <li><a class="dropdown-item" href="#" id="logoutBtn"><i class="fas fa-sign-out-alt me-2"></i>Logout</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item text-danger" href="#" data-bs-toggle="modal" data-bs-target="#deleteAccountModal"><i class="fas fa-user-slash me-2"></i>Delete Account</a></li>
                </ul>
            </div>
        `;
        document.getElementById('logoutBtn').addEventListener('click', logout);
    }
}

function setupEventListeners() {
    // Category Navigation
    document.querySelectorAll('.category-nav .nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            document.querySelectorAll('.category-nav .nav-link').forEach(l => l.classList.remove('active'));
            e.target.classList.add('active');
            
            currentCategory = e.target.getAttribute('data-category');
            currentSearch = '';
            document.getElementById('searchInput').value = '';
            
            resetAndLoadNews();
        });
    });

    // Search
    const searchInput = document.getElementById('searchInput');
    const searchDropdown = document.getElementById('searchHistoryDropdown');
    
    searchInput.addEventListener('focus', () => {
        if (searchHistory.length > 0) {
            searchDropdown.classList.add('d-block');
            searchDropdown.classList.remove('d-none');
            renderSearchHistory();
        }
    });

    document.addEventListener('click', (e) => {
        if (!document.getElementById('searchContainer').contains(e.target)) {
            searchDropdown.classList.remove('d-block');
            searchDropdown.classList.add('d-none');
        }
    });

    document.getElementById('searchForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const keyword = searchInput.value.trim();
        if (keyword) {
            currentSearch = keyword;
            currentCategory = 'Search';
            
            // Add to history
            addToSearchHistory(keyword);
            searchDropdown.classList.remove('d-block');
            searchDropdown.classList.add('d-none');
            
            document.querySelectorAll('.category-nav .nav-link').forEach(l => l.classList.remove('active'));
            resetAndLoadNews();
        }
    });

    document.getElementById('clearHistoryBtn').addEventListener('click', (e) => {
        e.preventDefault();
        searchHistory = [];
        localStorage.removeItem('globalpulse_search_history');
        renderSearchHistory();
        searchDropdown.classList.remove('d-block');
        searchDropdown.classList.add('d-none');
    });

    // Theme Toggle
    document.getElementById('themeToggleBtn').addEventListener('click', toggleTheme);

    // Pagination
    document.getElementById('prevPageBtn').addEventListener('click', () => {
        if (!isLoading && currentPage > 0) {
            currentPage--;
            loadNewsBasedOnState();
            window.scrollTo(0, 0);
        }
    });

    document.getElementById('nextPageBtn').addEventListener('click', () => {
        if (!isLoading) {
            currentPage++;
            loadNewsBasedOnState();
            window.scrollTo(0, 0);
        }
    });

    // Auth Forms
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    document.getElementById('resetPasswordForm').addEventListener('submit', handleResetPassword);
    document.getElementById('forgotUsernameForm').addEventListener('submit', handleForgotUsername);
    document.getElementById('deleteAccountForm').addEventListener('submit', handleDeleteAccount);
}

// Theme Logic
function initTheme() {
    const savedTheme = localStorage.getItem('globalpulse_theme') || 'dark';
    document.documentElement.setAttribute('data-bs-theme', savedTheme);
    updateThemeIcon(savedTheme);
}

function toggleTheme() {
    const currentTheme = document.documentElement.getAttribute('data-bs-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    
    document.documentElement.setAttribute('data-bs-theme', newTheme);
    localStorage.setItem('globalpulse_theme', newTheme);
    
    updateThemeIcon(newTheme);
    
    // Update navbar and modals dynamically based on theme
    const isDark = newTheme === 'dark';
    
    const navbar = document.querySelector('.navbar');
    if (isDark) {
        navbar.classList.add('navbar-dark', 'bg-dark');
        navbar.classList.remove('navbar-light', 'bg-light');
    } else {
        navbar.classList.add('navbar-light', 'bg-light');
        navbar.classList.remove('navbar-dark', 'bg-dark');
    }
}

function updateThemeIcon(theme) {
    const icon = document.getElementById('themeIcon');
    const btn = document.getElementById('themeToggleBtn');
    
    if (theme === 'dark') {
        icon.className = 'fas fa-sun';
        btn.classList.replace('btn-outline-dark', 'btn-outline-warning');
    } else {
        icon.className = 'fas fa-moon';
        btn.classList.replace('btn-outline-warning', 'btn-outline-dark');
    }
}

// Search History Logic
function initSearchHistory() {
    // History is loaded globally
}

function addToSearchHistory(keyword) {
    searchHistory = searchHistory.filter(k => k.toLowerCase() !== keyword.toLowerCase());
    searchHistory.unshift(keyword);
    if (searchHistory.length > 5) {
        searchHistory.pop();
    }
    localStorage.setItem('globalpulse_search_history', JSON.stringify(searchHistory));
}

function renderSearchHistory() {
    const list = document.getElementById('searchHistoryList');
    list.innerHTML = '';
    
    if (searchHistory.length === 0) {
        list.innerHTML = '<div class="px-3 py-2 text-muted small">No recent searches</div>';
        return;
    }
    
    searchHistory.forEach(keyword => {
        list.innerHTML += `
            <a class="dropdown-item px-3 py-2 d-flex align-items-center transition-all" href="#" onclick="triggerSearch('${keyword.replace(/'/g, "\\'")}')">
                <i class="fas fa-search text-muted me-3 small"></i>
                <span class="flex-grow-1">${keyword}</span>
                <i class="fas fa-arrow-right text-muted small opacity-50"></i>
            </a>
        `;
    });
}

window.triggerSearch = function(keyword) {
    document.getElementById('searchInput').value = keyword;
    document.getElementById('searchForm').dispatchEvent(new Event('submit'));
}

function resetAndLoadNews() {
    currentPage = 0;
    document.getElementById('newsContainer').innerHTML = '';
    
    let title = currentCategory === 'Home' ? 'Latest Updates' : 
               currentCategory === 'Search' ? `Search Results for "${currentSearch}"` : 
               `${currentCategory} News`;
               
    document.getElementById('mainSectionTitle').innerHTML = `<i class="fas fa-newspaper text-info me-2"></i>${title}`;
    
    if(currentCategory === 'Home') {
        document.getElementById('trendingSection').style.display = 'flex';
    } else {
        document.getElementById('trendingSection').style.display = 'none';
    }

    loadNewsBasedOnState();
}

function loadNewsBasedOnState() {
    if (currentCategory === 'Home') {
        fetchNews(`${API_BASE}/news/latest?page=${currentPage}&size=10`);
    } else if (currentCategory === 'Search') {
        fetchNews(`${API_BASE}/news/search?keyword=${encodeURIComponent(currentSearch)}&page=${currentPage}&size=10`);
    } else {
        fetchNews(`${API_BASE}/news/category/${encodeURIComponent(currentCategory)}?page=${currentPage}&size=10`);
    }
}

async function fetchNews(url) {
    if (apiCache.has(url)) {
        const data = apiCache.get(url);
        renderNews(data.content, 'newsContainer', currentPage > 0);
        
        const loadMoreGroup = document.getElementById('paginationGroup');
        if (data.last) {
            document.getElementById('nextPageBtn').classList.add('disabled');
        } else {
            document.getElementById('nextPageBtn').classList.remove('disabled');
        }
        
        if (currentPage === 0) {
            document.getElementById('prevPageBtn').classList.add('disabled');
        } else {
            document.getElementById('prevPageBtn').classList.remove('disabled');
        }
        
        return;
    }

    showLoading(true);
    try {
        const response = await fetch(url);
        const data = await response.json();
        apiCache.set(url, data);
        renderNews(data.content, 'newsContainer', currentPage > 0);
        
        const loadMoreGroup = document.getElementById('paginationGroup');
        if (data.last) {
            document.getElementById('nextPageBtn').classList.add('disabled');
        } else {
            document.getElementById('nextPageBtn').classList.remove('disabled');
        }
        
        if (currentPage === 0) {
            document.getElementById('prevPageBtn').classList.add('disabled');
        } else {
            document.getElementById('prevPageBtn').classList.remove('disabled');
        }
    } catch (error) {
        console.error('Error fetching news:', error);
    } finally {
        showLoading(false);
    }
}

async function loadTrending() {
    const url = `${API_BASE}/news/trending?page=0&size=4`;
    if (apiCache.has(url)) {
        const data = apiCache.get(url);
        renderNews(data.content, 'trendingContainer', false, 'col-md-6 col-lg-3');
        return;
    }
    try {
        const response = await fetch(url);
        const data = await response.json();
        apiCache.set(url, data);
        renderNews(data.content, 'trendingContainer', false, 'col-md-6 col-lg-3');
    } catch (error) {
        console.error('Error fetching trending news:', error);
    }
}

function loadLatest() {
    fetchNews(`${API_BASE}/news/latest?page=0&size=10`);
}

async function loadBreaking() {
    const url = `${API_BASE}/news?page=0&size=5`;
    if (apiCache.has(url)) {
        renderBreakingNews(apiCache.get(url).content);
        return;
    }
    try {
        // Just fetching some latest news and displaying minimally
        const response = await fetch(url);
        const data = await response.json();
        apiCache.set(url, data);
        renderBreakingNews(data.content);
    } catch (error) {
        console.error('Error fetching breaking news:', error);
    }
}

function renderBreakingNews(newsList) {
    const container = document.getElementById('breakingContainer');
    container.innerHTML = '';
    
    const uniqueAlerts = new Set();
    
    newsList.forEach(news => {
        if (uniqueAlerts.has(news.title)) return;
        uniqueAlerts.add(news.title);
        
        const dateObj = new Date(news.publishedAt || new Date());
        const timeAgoStr = Math.floor((new Date() - dateObj) / 60000) < 60 ? Math.floor((new Date() - dateObj) / 60000) + 'm ago' : dateObj.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
        container.innerHTML += `
            <li class="list-group-item px-3 py-3 border-secondary">
                <div class="d-flex align-items-center mb-1">
                    <span class="badge bg-danger me-2"><i class="fas fa-circle rounded-circle me-1" style="font-size: 6px; vertical-align: middle;"></i> Live ${timeAgoStr}</span>
                    <small class="text-muted">${news.source}</small>
                </div>
                <a href="${news.articleUrl}" target="_blank" class="fw-bold d-block text-warning">${news.title}</a>
            </li>
        `;
    });
}

function renderNews(newsList, containerId, append = false, colClass = 'col-md-6 mb-4') {
    const container = document.getElementById(containerId);
    if (!append) {
        container.innerHTML = '';
        if (containerId === 'newsContainer') {
            seenNewsTitles.clear();
        }
    }
    
    if (newsList.length === 0 && !append) {
        container.innerHTML = '<div class="col-12 text-center text-muted py-5">No news found.</div>';
        return;
    }
    
    const batchSeen = new Set();
    
    newsList.forEach(news => {
        // Handle deduplication and item selection
        const item = news.news ? news.news : news;
        
        // Deduplication logic
        if (containerId === 'newsContainer') {
            if (seenNewsTitles.has(item.title)) return;
            seenNewsTitles.add(item.title);
        } else {
            if (batchSeen.has(item.title)) return;
            batchSeen.add(item.title);
        }
        
        // Handle real-time relative dates
        const dateObj = new Date(item.publishedAt || item.createdAt);
        const timeAgo = (date) => {
            const seconds = Math.floor((new Date() - date) / 1000);
            let interval = seconds / 31536000;
            if (interval > 1) return Math.floor(interval) + " years ago";
            interval = seconds / 2592000;
            if (interval > 1) return Math.floor(interval) + " months ago";
            interval = seconds / 86400;
            if (interval > 1) return Math.floor(interval) + " days ago";
            interval = seconds / 3600;
            if (interval > 1) return Math.floor(interval) + " hours ago";
            interval = seconds / 60;
            if (interval > 1) return Math.floor(interval) + " minutes ago";
            return "Just now";
        };
        const dateStr = timeAgo(dateObj);
        
        // Handle images: Use article image if available, otherwise use a vibrant placeholder
        let imgUrl = item.imageUrl;
        if (!imgUrl || imgUrl.trim() === '' || imgUrl === 'null') {
            const colors = ['#0d6efd', '#198754', '#dc3545', '#ffc107', '#0dcaf0', '#6610f2', '#fd7e14'];
            const index = (item.id !== undefined && item.id !== null) ? (item.id % colors.length) : Math.floor(Math.random() * colors.length);
            const fillColor = colors[index].replace('#', '%23');
            // Fully URL-encoded SVG for maximum browser compatibility
            imgUrl = `data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22600%22%20height%3D%22400%22%3E%3Crect%20width%3D%22600%22%20height%3D%22400%22%20fill%3D%22${fillColor}%22%2F%3E%3C%2Fsvg%3E`;
        }
        
        // Handle descriptions
        let desc = item.description || '';
        if (desc.trim() === '') {
            desc = 'Click to read the full story on ' + item.source;
        }
        desc = desc.length > 100 ? desc.substring(0, 100) + '...' : desc;
        
        // Simple Sentiment Analysis based on keywords
        let sentimentClass = 'bg-secondary';
        let sentimentText = 'Neutral';
        let sentimentIcon = 'fa-minus';
        const posWords = ['win', 'won', 'success', 'boom', 'surge', 'rise', 'growth', 'positive', 'good', 'great', 'best', 'profit'];
        const negWords = ['crash', 'fail', 'loss', 'down', 'bad', 'worst', 'negative', 'decline', 'drop', 'crisis', 'war', 'death'];
        const titleLower = item.title.toLowerCase();
        
        if (posWords.some(w => titleLower.includes(w))) {
            sentimentClass = 'bg-success';
            sentimentText = 'Positive';
            sentimentIcon = 'fa-arrow-trend-up';
        } else if (negWords.some(w => titleLower.includes(w))) {
            sentimentClass = 'bg-danger';
            sentimentText = 'Negative';
            sentimentIcon = 'fa-arrow-trend-down';
        }
        
        const cardHtml = `
            <div class="${colClass}">
                <div class="card h-100 shadow-sm border-secondary">
                    <div class="position-relative">
                        <img src="${imgUrl}" class="card-img-top" alt="News Image" loading="lazy" onerror="this.onerror=null; this.src='data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22600%22%20height%3D%22400%22%3E%3Crect%20width%3D%22600%22%20height%3D%22400%22%20fill%3D%22%236c757d%22%2F%3E%3C%2Fsvg%3E'">
                        <span class="badge rounded-pill category-badge text-light">${item.category}</span>
                    </div>
                    <div class="card-body d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <span class="source-text"><i class="fas fa-rss me-1"></i>${item.source}</span>
                            <span class="source-text"><i class="far fa-clock me-1 text-warning"></i>${dateStr}</span>
                        </div>
                        <div class="mb-2">
                            <span class="badge ${sentimentClass}"><i class="fas ${sentimentIcon} me-1"></i>${sentimentText}</span>
                        </div>
                        <h5 class="news-card-title mb-3">
                            <a href="${item.articleUrl}" target="_blank">${item.title}</a>
                        </h5>
                        <p class="card-text text-muted small flex-grow-1">${desc}</p>
                        <div class="mt-auto d-flex justify-content-between align-items-center border-top border-secondary pt-3 mt-3">
                            <a href="${item.articleUrl}" target="_blank" class="btn btn-sm btn-outline-warning rounded-pill">Read More</a>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.insertAdjacentHTML('beforeend', cardHtml);
    });
}

function showLoading(show) {
    isLoading = show;
    document.getElementById('loadingSpinner').classList.toggle('d-none', !show);
    document.getElementById('prevPageBtn').disabled = show;
    document.getElementById('nextPageBtn').disabled = show;
}

// Authentication Logic
async function handleLogin(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    btn.disabled = true;
    
    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: document.getElementById('loginUsername').value,
                password: document.getElementById('loginPassword').value
            })
        });
        
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('globalpulse_token', data.token);
            localStorage.setItem('globalpulse_refresh_token', data.refreshToken);
            localStorage.setItem('globalpulse_username', data.username);
            userToken = data.token;
            refreshToken = data.refreshToken;
            username = data.username;
            
            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            initAuth();
            e.target.reset();
        } else {
            document.getElementById('loginError').textContent = 'Invalid credentials';
            document.getElementById('loginError').classList.remove('d-none');
        }
    } catch (err) {
        console.error(err);
    } finally {
        btn.disabled = false;
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    btn.disabled = true;
    
    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: document.getElementById('regUsername').value,
                email: document.getElementById('regEmail').value,
                password: document.getElementById('regPassword').value
            })
        });
        
        const data = await response.json();
        if (response.ok) {
            document.getElementById('registerSuccess').classList.remove('d-none');
            document.getElementById('registerError').classList.add('d-none');
            setTimeout(() => {
                bootstrap.Modal.getInstance(document.getElementById('registerModal')).hide();
                new bootstrap.Modal(document.getElementById('loginModal')).show();
                e.target.reset();
            }, 1500);
        } else {
            document.getElementById('registerError').textContent = data.message || 'Registration failed';
            document.getElementById('registerError').classList.remove('d-none');
            document.getElementById('registerSuccess').classList.add('d-none');
        }
    } catch (err) {
        console.error(err);
    } finally {
        btn.disabled = false;
    }
}

async function handleResetPassword(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const errorDiv = document.getElementById('resetPasswordError');
    const successDiv = document.getElementById('resetPasswordSuccess');
    btn.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/auth/reset-password`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                username: document.getElementById('resetUsername').value,
                email: document.getElementById('resetEmail').value,
                newPassword: document.getElementById('resetNewPassword').value
            })
        });

        const data = await response.json();
        if (response.ok) {
            successDiv.textContent = data.message;
            successDiv.classList.remove('d-none');
            errorDiv.classList.add('d-none');
            setTimeout(() => {
                bootstrap.Modal.getInstance(document.getElementById('resetPasswordModal')).hide();
                new bootstrap.Modal(document.getElementById('loginModal')).show();
                e.target.reset();
            }, 2000);
        } else {
            errorDiv.textContent = data.message || 'Failed to reset password';
            errorDiv.classList.remove('d-none');
            successDiv.classList.add('d-none');
        }
    } catch (err) {
        console.error(err);
    } finally {
        btn.disabled = false;
    }
}

async function handleForgotUsername(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const errorDiv = document.getElementById('forgotUsernameError');
    const successDiv = document.getElementById('forgotUsernameSuccess');
    btn.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/auth/forgot-username`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                email: document.getElementById('forgotEmail').value
            })
        });

        const data = await response.json();
        if (response.ok) {
            successDiv.textContent = data.message;
            successDiv.classList.remove('d-none');
            errorDiv.classList.add('d-none');
            // We don't hide the modal automatically so they can see the username
        } else {
            errorDiv.textContent = data.message || 'Failed to retrieve username';
            errorDiv.classList.remove('d-none');
            successDiv.classList.add('d-none');
        }
    } catch (err) {
        console.error(err);
    } finally {
        btn.disabled = false;
    }
}

async function handleDeleteAccount(e) {
    e.preventDefault();
    const btn = e.target.querySelector('button');
    const errorDiv = document.getElementById('deleteAccountError');
    btn.disabled = true;

    try {
        const response = await fetch(`${API_BASE}/auth/delete-account`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${userToken}`
            },
            body: JSON.stringify({
                username: document.getElementById('deleteUsername').value,
                password: document.getElementById('deletePassword').value
            })
        });

        if (response.ok) {
            bootstrap.Modal.getInstance(document.getElementById('deleteAccountModal')).hide();
            alert('Your account has been permanently deleted.');
            logout();
        } else {
            const data = await response.json();
            errorDiv.textContent = data.message || 'Failed to delete account';
            errorDiv.classList.remove('d-none');
        }
    } catch (err) {
        console.error(err);
    } finally {
        btn.disabled = false;
    }
}

function logout() {
    localStorage.removeItem('globalpulse_token');
    localStorage.removeItem('globalpulse_refresh_token');
    localStorage.removeItem('globalpulse_username');
    userToken = null;
    refreshToken = null;
    username = null;
    window.location.reload();
}

async function refreshAccessToken() {
    if (!refreshToken) return false;
    try {
        const response = await fetch(`${API_BASE}/auth/refreshtoken`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({ refreshToken: refreshToken })
        });
        if (response.ok) {
            const data = await response.json();
            localStorage.setItem('globalpulse_token', data.accessToken);
            userToken = data.accessToken;
            return true;
        }
    } catch (e) {
        console.error('Refresh token failed:', e);
    }
    logout();
    return false;
}
