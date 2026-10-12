/**
 * =====================================================================
 * ORBIT SOCIAL PLATFORM - CLIENT CONTROLLER
 * Architecture: Vanilla JavaScript SPA connecting to Jakarta Servlets REST API
 * =====================================================================
 */

// -------------------------------------------------------------
// 1. APPLICATION STATE
// -------------------------------------------------------------
let currentUser = null;
let activeChatFriendId = null;
let activeChatPollingInterval = null;

// DOM Elements Cache
const authModal = document.getElementById('auth-modal');
const appDashboard = document.getElementById('app-dashboard');
const landingHero = document.getElementById('landing-hero');
const userMenuArea = document.getElementById('user-menu-area');
const toastContainer = document.getElementById('toast-container');

// Application Bootstrap
document.addEventListener('DOMContentLoaded', () => {
    initInterestChips();
    checkAuthSession();
});

// -------------------------------------------------------------
// 2. UI HELPERS & NOTIFICATIONS
// -------------------------------------------------------------
function showToast(message) {
    if (!toastContainer) return;
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.textContent = message;
    toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// -------------------------------------------------------------
// 3. AUTHENTICATION & SESSION MANAGEMENT
// -------------------------------------------------------------
async function checkAuthSession() {
    try {
        const res = await fetch('api/auth?action=me');
        if (res.ok) {
            const data = await res.json();
            if (data.success && data.user) {
                onUserLoggedIn(data.user);
                return;
            }
        }
    } catch (err) {
        console.warn('No active session found.', err);
    }
    onUserLoggedOut();
}

// Guest Guard Helper
function checkGuestGuard(actionName) {
    if (currentUser && (currentUser.isGuest || currentUser.id <= 0)) {
        showToast(`🔒 ${actionName} requires an account. Please sign in or register!`);
        openAuthModal('register');
        return true;
    }
    return false;
}

function onUserLoggedIn(user) {
    currentUser = user;
    if (authModal) authModal.style.display = 'none';
    if (landingHero) landingHero.style.display = 'none';
    if (appDashboard) appDashboard.style.display = 'block';

    const guestBanner = document.getElementById('guest-mode-banner');
    const isGuest = user.isGuest || user.id <= 0;
    if (guestBanner) {
        guestBanner.style.display = isGuest ? 'flex' : 'none';
    }

    if (userMenuArea) {
        const initial = user.name ? user.name.charAt(0).toUpperCase() : 'G';
        userMenuArea.innerHTML = `
            <div class="user-nav-chip">
                <div class="user-avatar user-avatar-sm" style="${isGuest ? 'background: linear-gradient(135deg, #06b6d4, #6366f1);' : ''}">
                    ${escapeHtml(initial)}
                </div>
                <div class="user-nav-info">
                    <div class="user-nav-name">${escapeHtml(user.name)}</div>
                    <div class="user-nav-status">${isGuest ? 'Guest Mode' : 'In Orbit'}</div>
                </div>
                ${isGuest ? `
                    <button class="btn btn-sm btn-primary" onclick="openAuthModal('register')" style="margin-left: 6px; font-size: 0.8rem; padding: 4px 10px;">Sign Up</button>
                    <button class="btn btn-sm btn-outline" onclick="logout()" style="margin-left: 4px; font-size: 0.8rem; padding: 4px 10px;">Exit</button>
                ` : `
                    <button class="btn btn-sm btn-outline" onclick="logout()" style="margin-left: 6px;">Sign Out</button>
                `}
            </div>
        `;
    }

    // Populate Profile Fields
    const pAvatarLetter = document.getElementById('prof-avatar-letter');
    const pName = document.getElementById('prof-name');
    const pEmail = document.getElementById('prof-email');
    const pBio = document.getElementById('prof-bio');
    const pInterests = document.getElementById('prof-interests');

    if (pAvatarLetter) pAvatarLetter.textContent = user.name ? user.name.charAt(0).toUpperCase() : 'G';
    if (pName) pName.textContent = user.name + (isGuest ? ' (Guest)' : '');
    if (pEmail) pEmail.textContent = user.email;
    if (pBio) pBio.value = user.bio || '';
    if (pInterests) pInterests.value = user.interest || '';

    // Switch to initial tab: Discover / Match
    switchTab('match');
}

function onUserLoggedOut() {
    currentUser = null;
    if (appDashboard) appDashboard.style.display = 'none';
    if (landingHero) landingHero.style.display = 'block';

    const guestBanner = document.getElementById('guest-mode-banner');
    if (guestBanner) guestBanner.style.display = 'none';

    if (userMenuArea) {
        userMenuArea.innerHTML = `
            <button class="btn btn-outline" onclick="openAuthModal('login')">Sign In</button>
            <button class="btn btn-primary" onclick="openAuthModal('register')">Get In Orbit</button>
        `;
    }

    if (activeChatPollingInterval) {
        clearInterval(activeChatPollingInterval);
        activeChatPollingInterval = null;
    }
    activeChatFriendId = null;
}

function openAuthModal(mode) {
    if (!authModal) return;
    authModal.style.display = 'flex';
    switchAuthTab(mode);
}

function closeAuthModal() {
    if (authModal) authModal.style.display = 'none';
}

function switchAuthTab(tab) {
    const loginForm = document.getElementById('login-form-pane');
    const registerForm = document.getElementById('register-form-pane');
    const forgotForm = document.getElementById('forgot-form-pane');
    const tabLogin = document.getElementById('tab-btn-login');
    const tabRegister = document.getElementById('tab-btn-register');

    if (forgotForm) forgotForm.style.display = 'none';

    if (tab === 'login') {
        if (loginForm) loginForm.style.display = 'block';
        if (registerForm) registerForm.style.display = 'none';
        if (tabLogin) tabLogin.classList.add('active');
        if (tabRegister) tabRegister.classList.remove('active');
    } else {
        if (loginForm) loginForm.style.display = 'none';
        if (registerForm) registerForm.style.display = 'block';
        if (tabLogin) tabLogin.classList.remove('active');
        if (tabRegister) tabRegister.classList.add('active');
    }
}

function openForgotPasswordPane() {
    const loginForm = document.getElementById('login-form-pane');
    const registerForm = document.getElementById('register-form-pane');
    const forgotForm = document.getElementById('forgot-form-pane');
    const tabLogin = document.getElementById('tab-btn-login');
    const tabRegister = document.getElementById('tab-btn-register');

    if (loginForm) loginForm.style.display = 'none';
    if (registerForm) registerForm.style.display = 'none';
    if (forgotForm) forgotForm.style.display = 'block';
    if (tabLogin) tabLogin.classList.remove('active');
    if (tabRegister) tabRegister.classList.remove('active');

    // Pre-fill email from login form if typed
    const loginEmail = document.getElementById('login-email');
    const forgotEmail = document.getElementById('forgot-email');
    if (loginEmail && forgotEmail && loginEmail.value.trim()) {
        forgotEmail.value = loginEmail.value.trim();
    }
}

async function loginAsGuest() {
    try {
        const res = await fetch('api/auth?action=guestLogin', {
            method: 'POST'
        });
        const data = await res.json();
        if (res.ok && data.success && data.user) {
            showToast('👋 Welcome, Guest Explorer! Previewing Orbit.');
            onUserLoggedIn(data.user);
        } else {
            showToast('⚠️ Unable to start guest session.');
        }
    } catch (err) {
        showToast('⚠️ Server connection error.');
    }
}

async function handleForgotPasswordForm(e) {
    e.preventDefault();
    const email = document.getElementById('forgot-email').value.trim();
    const newPassword = document.getElementById('forgot-password').value;

    const formData = new URLSearchParams();
    formData.append('email', email);
    formData.append('newPassword', newPassword);

    try {
        const res = await fetch('api/auth?action=forgotPassword', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });

        const data = await res.json();
        if (res.ok && data.success) {
            showToast('✅ ' + (data.message || 'Password reset successfully!'));
            // Switch back to login form with email prefilled
            switchAuthTab('login');
            const loginEmail = document.getElementById('login-email');
            if (loginEmail) loginEmail.value = email;
            const loginPassword = document.getElementById('login-password');
            if (loginPassword) {
                loginPassword.value = '';
                loginPassword.focus();
            }
        } else {
            showToast('⚠️ ' + (data.message || 'Could not reset password.'));
        }
    } catch (err) {
        showToast('⚠️ Error connecting to server.');
    }
}

async function handleLoginForm(e) {
    e.preventDefault();
    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    try {
        const formData = new URLSearchParams();
        formData.append('email', email);
        formData.append('password', password);

        const res = await fetch('api/auth?action=login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });

        const data = await res.json();
        if (res.ok && data.success) {
            showToast(data.message || 'Welcome to Orbit!');
            onUserLoggedIn(data.user);
        } else {
            showToast('⚠️ ' + (data.message || 'Invalid credentials.'));
        }
    } catch (err) {
        showToast('⚠️ Unable to connect to Orbit server.');
    }
}

async function handleRegisterForm(e) {
    e.preventDefault();
    const name = document.getElementById('reg-name').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const bio = document.getElementById('reg-bio').value.trim();
    const interest = document.getElementById('reg-interest').value.trim();

    const formData = new URLSearchParams();
    formData.append('name', name);
    formData.append('email', email);
    formData.append('password', password);
    formData.append('bio', bio);
    formData.append('interest', interest);

    try {
        const res = await fetch('api/auth?action=register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });

        const data = await res.json();
        if (res.ok && data.success) {
            showToast('🎉 Welcome to Orbit! Account created.');
            if (data.user) {
                onUserLoggedIn(data.user);
            } else {
                switchAuthTab('login');
            }
        } else {
            showToast('⚠️ ' + (data.message || 'Registration failed.'));
        }
    } catch (err) {
        showToast('⚠️ Error connecting to server.');
    }
}

async function logout() {
    try {
        await fetch('api/auth?action=logout', { method: 'POST' });
        showToast('Logged out of Orbit.');
        onUserLoggedOut();
    } catch (e) {
        onUserLoggedOut();
    }
}

// -------------------------------------------------------------
// 4. INTEREST CHIPS TOGGLE
// -------------------------------------------------------------
function initInterestChips() {
    const chips = document.querySelectorAll('.selectable-chip');
    const input = document.getElementById('reg-interest');
    if (!chips.length || !input) return;

    chips.forEach(chip => {
        chip.addEventListener('click', () => {
            chip.classList.toggle('selected');
            const selected = [];
            document.querySelectorAll('.selectable-chip.selected').forEach(c => {
                selected.push(c.getAttribute('data-tag'));
            });
            input.value = selected.join(', ');
        });
    });
}

// -------------------------------------------------------------
// 5. DASHBOARD TABS SWITCHING
// -------------------------------------------------------------
function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(pane => pane.classList.remove('active'));

    const btn = document.getElementById(`tab-btn-${tabName}`);
    const pane = document.getElementById(`tab-pane-${tabName}`);
    if (btn) btn.classList.add('active');
    if (pane) pane.classList.add('active');

    if (tabName === 'match') {
        loadMatches();
    } else if (tabName === 'vibe') {
        loadFeed();
    } else if (tabName === 'circle') {
        loadCircleData();
    }
}

// -------------------------------------------------------------
// 6. ORBIT MATCH ENGINE & DISCOVERY
// -------------------------------------------------------------
async function loadMatches() {
    const grid = document.getElementById('matches-grid-container');
    if (!grid) return;
    grid.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">🪐</div>
            <div class="empty-state-title">Scanning Your Orbit</div>
            <div class="empty-state-desc">Calculating interest compatibility with other orbiters...</div>
        </div>
    `;

    try {
        const res = await fetch('api/match');
        const data = await res.json();
        if (data.success && data.matches) {
            renderMatches(data.matches);
        } else {
            renderMatches([]);
        }
    } catch (err) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <div class="empty-state-title">Connection Error</div>
                <div class="empty-state-desc">Unable to load matches. Please click refresh to retry.</div>
            </div>
        `;
    }
}

function renderMatches(matches) {
    const grid = document.getElementById('matches-grid-container');
    if (!matches || matches.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🚀</div>
                <div class="empty-state-title">No Orbiters Found Yet</div>
                <div class="empty-state-desc">
                    You're an early explorer in this orbit! Invite colleagues or friends to create an account and discover matching passions.
                </div>
            </div>
        `;
        return;
    }

    grid.innerHTML = matches.map(m => {
        const user = { id: m.userId, name: m.name, bio: m.bio, interest: m.interest };
        const score = m.matchPercentage || 0;
        const common = m.commonInterests || [];
        const status = m.connectionStatus || 'NONE';

        let buttonHtml = '';
        if (status === 'ACCEPTED') {
            buttonHtml = `<button class="btn btn-sm btn-outline" onclick="openChatWith(${user.id}, '${escapeHtml(user.name)}')">💬 Chat in Circle</button>`;
        } else if (status === 'PENDING_SENT') {
            buttonHtml = `<button class="btn btn-sm btn-outline" disabled style="opacity: 0.65;">⏳ Request Sent</button>`;
        } else if (status === 'PENDING_RECEIVED') {
            buttonHtml = `<button class="btn btn-sm btn-success" onclick="acceptConnection(${user.id})">✅ Accept Request</button>`;
        } else {
            buttonHtml = `<button class="btn btn-sm btn-primary" onclick="sendConnectionRequest(${user.id})">🪐 Connect</button>`;
        }

        const allInterests = (user.interest || '').split(',').map(s => s.trim()).filter(Boolean);

        return `
            <div class="match-card">
                <div>
                    <div class="match-card-header">
                        <div class="match-user-meta">
                            <div class="user-avatar">${escapeHtml(user.name.charAt(0).toUpperCase())}</div>
                            <div>
                                <h4 class="match-user-name">${escapeHtml(user.name)}</h4>
                                <div class="match-user-sub">${allInterests.length} Interests Stored</div>
                            </div>
                        </div>
                        <div class="match-score-badge">
                            <span class="score-num">${score}%</span>
                            <span class="score-label">Orbit Match</span>
                        </div>
                    </div>

                    <p class="match-bio">${escapeHtml(user.bio || 'Exploring new frequencies in Orbit.')}</p>

                    ${common.length > 0 ? `
                        <div class="common-interests-box">
                            <div class="common-label">⚡ Shared Passions (${common.length})</div>
                            <div class="tags-wrap">
                                ${common.map(tag => `<span class="chip-tag chip-match">★ ${escapeHtml(tag)}</span>`).join('')}
                            </div>
                        </div>
                    ` : `
                        <div class="common-interests-box" style="opacity: 0.6;">
                            <div class="common-label" style="color: var(--text-dim);">No shared interests yet</div>
                        </div>
                    `}

                    <div style="margin-top: 10px;">
                        <div style="font-size: 0.72rem; color: var(--text-dim); margin-bottom: 5px; text-transform: uppercase;">All Interests</div>
                        <div class="tags-wrap">
                            ${allInterests.map(tag => `<span class="chip-tag">${escapeHtml(tag)}</span>`).join('')}
                        </div>
                    </div>
                </div>

                <div class="match-card-actions">
                    ${buttonHtml}
                </div>
            </div>
        `;
    }).join('');
}

// -------------------------------------------------------------
// 7. MYVIBE SOCIAL FEED & COMMENTS
// -------------------------------------------------------------
async function loadFeed() {
    const container = document.getElementById('feed-posts-container');
    if (!container) return;
    try {
        const res = await fetch('api/feed');
        const data = await res.json();
        if (data.success && data.posts) {
            renderFeed(data.posts);
        }
    } catch (err) {
        container.innerHTML = `<div class="empty-state"><div class="empty-state-icon">⚠️</div><div class="empty-state-title">Error Loading Feed</div></div>`;
    }
}

function renderFeed(posts) {
    const container = document.getElementById('feed-posts-container');
    if (!posts || posts.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">✨</div>
                <div class="empty-state-title">No Vibes Shared Yet</div>
                <div class="empty-state-desc">Be the first to share an idea, project, or breakthrough with the orbit!</div>
            </div>
        `;
        return;
    }

    container.innerHTML = posts.map(p => `
        <article class="post-card" id="post-${p.postId}">
            <div class="post-header">
                <div class="user-avatar user-avatar-sm">
                    ${escapeHtml(p.authorName ? p.authorName.charAt(0).toUpperCase() : '?')}
                </div>
                <div>
                    <div class="post-author-name">${escapeHtml(p.authorName || 'Orbiter')}</div>
                    <div class="post-author-badge">Community Vibe</div>
                </div>
            </div>

            <div class="post-content">${escapeHtml(p.content)}</div>

            ${p.mediaUrl ? `
                <img src="${escapeHtml(p.mediaUrl)}" class="post-image" alt="Post media" onerror="this.style.display='none'">
            ` : ''}

            <!-- Comments Section -->
            <div class="post-comments">
                <div class="comments-count-label">
                    Comments (${p.comments ? p.comments.length : 0})
                </div>

                <div id="comments-list-${p.postId}">
                    ${(p.comments || []).map(c => `
                        <div class="comment-item">
                            <div class="user-avatar user-avatar-sm" style="width: 26px; height: 26px; font-size: 0.72rem;">
                                ${escapeHtml(c.authorName ? c.authorName.charAt(0).toUpperCase() : '?')}
                            </div>
                            <div class="comment-bubble">
                                <div class="comment-author">${escapeHtml(c.authorName)}</div>
                                <div>${escapeHtml(c.commentText)}</div>
                            </div>
                        </div>
                    `).join('')}
                </div>

                <div class="comment-input-row">
                    <input type="text" id="comment-input-${p.postId}" placeholder="Write a comment..." onkeydown="if(event.key==='Enter') submitComment(${p.postId})">
                    <button class="btn btn-sm btn-outline" onclick="submitComment(${p.postId})">Reply</button>
                </div>
            </div>
        </article>
    `).join('');
}

async function handleCreatePost(e) {
    e.preventDefault();
    if (checkGuestGuard('Publishing posts')) return;

    const content = document.getElementById('post-content').value;
    const mediaUrl = document.getElementById('post-media-url').value;

    if (!content.trim()) {
        showToast('Please type something to post.');
        return;
    }

    const formData = new URLSearchParams();
    formData.append('content', content);
    formData.append('mediaUrl', mediaUrl);

    try {
        const res = await fetch('api/feed?action=post', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
        const data = await res.json();
        if (res.ok && data.success) {
            document.getElementById('post-content').value = '';
            document.getElementById('post-media-url').value = '';
            showToast('✨ Vibe shared to the network!');
            loadFeed();
        } else {
            showToast('⚠️ ' + (data.message || 'Failed to post.'));
        }
    } catch (err) {
        showToast('⚠️ Error publishing post.');
    }
}

async function submitComment(postId) {
    if (checkGuestGuard('Commenting on posts')) return;

    const input = document.getElementById(`comment-input-${postId}`);
    if (!input || !input.value.trim()) return;

    const formData = new URLSearchParams();
    formData.append('postId', postId);
    formData.append('commentText', input.value.trim());

    try {
        const res = await fetch('api/feed?action=comment', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
        const data = await res.json();
        if (res.ok && data.success) {
            input.value = '';
            showToast('Comment added!');
            loadFeed();
        } else {
            showToast('⚠️ ' + (data.message || 'Failed to add comment.'));
        }
    } catch (err) {
        showToast('⚠️ Failed to add comment.');
    }
}

// -------------------------------------------------------------
// 8. CIRCLE NETWORK & 1-TO-1 CHAT
// -------------------------------------------------------------
async function sendConnectionRequest(targetUserId) {
    if (checkGuestGuard('Connecting with orbiters')) return;

    const formData = new URLSearchParams();
    formData.append('targetUserId', targetUserId);

    try {
        const res = await fetch('api/circle?action=connect', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
        const data = await res.json();
        if (res.ok && data.success) {
            showToast('🪐 Connection request sent!');
            loadMatches();
        } else {
            showToast('⚠️ ' + (data.message || 'Could not send request.'));
        }
    } catch (err) {
        showToast('⚠️ Error sending request.');
    }
}

async function acceptConnection(senderId) {
    if (checkGuestGuard('Accepting circle connections')) return;

    const formData = new URLSearchParams();
    formData.append('senderId', senderId);

    try {
        const res = await fetch('api/circle?action=accept', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
        const data = await res.json();
        if (res.ok && data.success) {
            showToast('🤝 Connection accepted! Added to your Circle.');
            loadMatches();
            loadCircleData();
        } else {
            showToast('⚠️ Error accepting connection.');
        }
    } catch (err) {
        showToast('⚠️ Error accepting connection.');
    }
}

async function loadCircleData() {
    loadPendingRequests();
    loadCircleFriends();
}

async function loadPendingRequests() {
    const container = document.getElementById('pending-requests-container');
    if (!container) return;

    try {
        const res = await fetch('api/circle?action=requests');
        const data = await res.json();
        if (data.success && data.requests && data.requests.length > 0) {
            container.innerHTML = data.requests.map(r => `
                <div class="friend-item" style="border-left: 3px solid var(--accent-indigo);">
                    <div>
                        <strong>${escapeHtml(r.senderName)}</strong>
                        <div style="font-size: 0.74rem; color: #38bdf8;">Wants to connect</div>
                    </div>
                    <button class="btn btn-sm btn-success" onclick="acceptConnection(${r.senderId})">Accept</button>
                </div>
            `).join('');
        } else {
            container.innerHTML = `<div style="font-size: 0.84rem; color: var(--text-dim); padding: 8px 0;">No pending requests.</div>`;
        }
    } catch (err) {
        console.error(err);
    }
}

async function loadCircleFriends() {
    const container = document.getElementById('circle-friends-container');
    if (!container) return;

    try {
        const res = await fetch('api/circle?action=friends');
        const data = await res.json();
        if (data.success && data.friends && data.friends.length > 0) {
            container.innerHTML = data.friends.map(f => `
                <div class="friend-item ${activeChatFriendId === f.id ? 'active' : ''}" onclick="openChatWith(${f.id}, '${escapeHtml(f.name)}')">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <div class="user-avatar user-avatar-sm">
                            ${escapeHtml(f.name.charAt(0).toUpperCase())}
                        </div>
                        <div>
                            <div style="font-weight: 600; font-size: 0.9rem;">${escapeHtml(f.name)}</div>
                            <div style="font-size: 0.74rem; color: var(--text-dim);">${escapeHtml(f.interest || '')}</div>
                        </div>
                    </div>
                    <span style="font-size: 0.85rem;">💬</span>
                </div>
            `).join('');

            // Automatically open chat with first friend if none active
            if (!activeChatFriendId) {
                openChatWith(data.friends[0].id, data.friends[0].name);
            }
        } else {
            container.innerHTML = `<div style="font-size: 0.84rem; color: var(--text-dim); padding: 8px 0;">Your Circle is empty. Match in Discover!</div>`;
            document.getElementById('chat-window').innerHTML = `
                <div class="empty-state">
                    <div class="empty-state-icon">🪐</div>
                    <h3 class="empty-state-title">No Connected Friends Yet</h3>
                    <p class="empty-state-desc">Discover orbiters with matching interests & send connection requests to start conversations!</p>
                </div>
            `;
        }
    } catch (err) {
        console.error(err);
    }
}

function openChatWith(friendId, friendName) {
    activeChatFriendId = friendId;
    switchTab('circle');

    const chatWindow = document.getElementById('chat-window');
    chatWindow.innerHTML = `
        <div class="chat-header">
            <div class="user-avatar user-avatar-sm">
                ${escapeHtml(friendName.charAt(0).toUpperCase())}
            </div>
            <div>
                <strong style="font-size: 0.95rem;">${escapeHtml(friendName)}</strong>
                <div style="font-size: 0.72rem; color: var(--accent-green);">● Active in Orbit</div>
            </div>
        </div>

        <div class="chat-messages" id="chat-messages-container">
            <div style="color: var(--text-dim); text-align: center; padding: 20px;">Loading conversation...</div>
        </div>

        <div class="chat-input-row">
            <input type="text" id="chat-text-input" placeholder="Type a message..." onkeydown="if(event.key==='Enter') sendChatMessage()">
            <button class="btn btn-primary" onclick="sendChatMessage()">Send</button>
        </div>
    `;

    fetchConversation();

    if (activeChatPollingInterval) clearInterval(activeChatPollingInterval);
    activeChatPollingInterval = setInterval(fetchConversation, 3000);
}

async function fetchConversation() {
    if (!activeChatFriendId) return;
    const container = document.getElementById('chat-messages-container');
    if (!container) return;

    try {
        const res = await fetch(`api/messages?userId=${activeChatFriendId}`);
        const data = await res.json();
        if (data.success && data.messages) {
            if (data.messages.length === 0) {
                container.innerHTML = `<div style="color: var(--text-dim); text-align: center; margin-top: 50px;">No messages yet. Say hello! 👋</div>`;
                return;
            }

            container.innerHTML = data.messages.map(m => {
                const isMine = (m.senderId === currentUser.id);
                return `
                    <div class="message-bubble ${isMine ? 'message-outgoing' : 'message-incoming'}">
                        ${escapeHtml(m.messageText)}
                    </div>
                `;
            }).join('');

            container.scrollTop = container.scrollHeight;
        }
    } catch (e) {
        console.error('Error fetching chat messages', e);
    }
}

async function sendChatMessage() {
    if (checkGuestGuard('Sending direct messages')) return;

    const input = document.getElementById('chat-text-input');
    if (!input || !input.value.trim() || !activeChatFriendId) return;

    const text = input.value.trim();
    input.value = '';

    const formData = new URLSearchParams();
    formData.append('receiverId', activeChatFriendId);
    formData.append('messageText', text);

    try {
        const res = await fetch('api/messages', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
        const data = await res.json();
        if (res.ok && data.success) {
            fetchConversation();
        } else {
            showToast('⚠️ Failed to send message.');
        }
    } catch (err) {
        showToast('⚠️ Error sending message.');
    }
}

// -------------------------------------------------------------
// 9. PROFILE MANAGEMENT
// -------------------------------------------------------------
async function handleUpdateProfile(e) {
    e.preventDefault();
    if (checkGuestGuard('Updating profile')) return;

    const bio = document.getElementById('prof-bio').value;
    const interest = document.getElementById('prof-interests').value;

    const formData = new URLSearchParams();
    formData.append('bio', bio);
    formData.append('interest', interest);

    try {
        const res = await fetch('api/auth?action=updateProfile', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: formData.toString()
        });
        const data = await res.json();
        if (res.ok && data.success) {
            currentUser.bio = bio;
            currentUser.interest = interest;
            showToast('✅ Profile updated! Recalculating Orbit match scores...');
            loadMatches();
        } else {
            showToast('⚠️ ' + (data.message || 'Update failed.'));
        }
    } catch (err) {
        showToast('⚠️ Error updating profile.');
    }
}

// -------------------------------------------------------------
// 10. XSS SECURITY SANITIZER
// -------------------------------------------------------------
function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
