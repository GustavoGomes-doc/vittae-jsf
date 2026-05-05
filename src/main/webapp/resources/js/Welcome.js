function showPage(pageId) {
    const pages = document.querySelectorAll('.page');
    pages.forEach(page => {
        page.classList.remove('active');
    });


    const targetPage = document.getElementById(pageId + '-page');
    if (targetPage) {
        targetPage.classList.add('active');
    }

    updateNavigation(pageId);
    closeMobileMenu();

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function updateNavigation(activePageId) {
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        link.classList.remove('active');
    });

    const activeLink = document.querySelector(`[onclick="showPage('${activePageId}')"]`);
    if (activeLink && activeLink.classList.contains('nav-link')) {
        activeLink.classList.add('active');
    }
}

function toggleMobileMenu() {
    const mobileMenu = document.getElementById('mobileMenu');
    mobileMenu.classList.toggle('active');
}

function closeMobileMenu() {
    const mobileMenu = document.getElementById('mobileMenu');
    mobileMenu.classList.remove('active');
}

function smoothScroll(targetId) {
    const target = document.getElementById(targetId);
    if (target) {
        target.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

function observeElements() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.animationPlayState = 'running';
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });

    const animatedElements = document.querySelectorAll('.feature-card, .content-card');
    animatedElements.forEach(el => {
        observer.observe(el);
    });
}

function handleHeaderScroll() {
    const header = document.querySelector('.header');
    let lastScrollTop = 0;

    window.addEventListener('scroll', () => {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        
        if (scrollTop > lastScrollTop && scrollTop > 100) {
            header.style.transform = 'translateY(-100%)';
        } else {
            header.style.transform = 'translateY(0)';
        }
        
        lastScrollTop = scrollTop;
    });
}

function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;

    const requiredFields = form.querySelectorAll('[required]');
    let isValid = true;

    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            field.classList.add('error');
            isValid = false;
        } else {
            field.classList.remove('error');
        }
    });

    return isValid;
}

// toats notificações sistemas
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;

    // estilo do toast
    toast.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'success' ? '#10b981' : type === 'error' ? '#ef4444' : '#3b82f6'};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 0.5rem;
        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        z-index: 10000;
        animation: slideInRight 0.3s ease-out;
        max-width: 300px;
        word-wrap: break-word;
    `;

    document.body.appendChild(toast);

    // remover toast 3 segundos
    setTimeout(() => {
        toast.style.animation = 'slideOutRight 0.3s ease-out';
        setTimeout(() => {
            if (document.body.contains(toast)) {
                document.body.removeChild(toast);
            }
        }, 300);
    }, 3000);
}

// css toast
function addToastStyles() {
    if (document.getElementById('toast-styles')) return;

    const style = document.createElement('style');
    style.id = 'toast-styles';
    style.textContent = `   
        @keyframes slideInRight {
            from {
                opacity: 0;
                transform: translateX(100%);
            }
            to {
                opacity: 1;
                transform: translateX(0);
            }
        }
        
        @keyframes slideOutRight {
            from {
                opacity: 1;
                transform: translateX(0);
            }
            to {
                opacity: 0;
                transform: translateX(100%);
            }
        }
    `;
    document.head.appendChild(style);
}

//loading
function showLoading(elementId) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = '<div class="loading-spinner">Carregando...</div>';
        element.style.opacity = '0.6';
        element.style.pointerEvents = 'none';
    }
}

function hideLoading(elementId, originalContent) {
    const element = document.getElementById(elementId);
    if (element) {
        element.innerHTML = originalContent;
        element.style.opacity = '1';
        element.style.pointerEvents = 'auto';
    }
}

// Local Storage Utilities
function saveToStorage(key, data) {
    try {
        localStorage.setItem(key, JSON.stringify(data));
        return true;
    } catch (error) {
        console.error('Error saving to localStorage:', error);
        return false;
    }
}

function loadFromStorage(key) {
    try {
        const data = localStorage.getItem(key);
        return data ? JSON.parse(data) : null;
    } catch (error) {
        console.error('Error loading from localStorage:', error);
        return null;
    }
}

// Device Detection
function isMobile() {
    return window.innerWidth <= 768;
}

function isTablet() {
    return window.innerWidth > 768 && window.innerWidth <= 1024;
}

// Debounce Function for Performance
function debounce(func, wait, immediate) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            timeout = null;
            if (!immediate) func(...args);
        };
        const callNow = immediate && !timeout;
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
        if (callNow) func(...args);
    };
}

// Initialize Application
function inicializar() {
    // Add toast styles
    addToastStyles();

    // Initialize observers
    observeElements();

    // Setup header scroll effect
    handleHeaderScroll();

    // Close mobile menu when clicking outside
    document.addEventListener('click', (e) => {
        const mobileMenu = document.getElementById('mobileMenu');
        const menuBtn = document.querySelector('.mobile-menu-btn');
        
        if (!mobileMenu.contains(e.target) && !menuBtn.contains(e.target)) {
            closeMobileMenu();
        }
    });

    // Handle window resize
    const debouncedResize = debounce(() => {
        // Close mobile menu on resize to desktop
        if (!isMobile()) {
            closeMobileMenu();
        }
    }, 250);

    window.addEventListener('resize', debouncedResize);

    // Keyboard navigation
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeMobileMenu();
        }
    });

    // Initialize with home page
    showPage('home');

    console.log('Vittae App Initialized Successfully! 🏥');
}

// Run initialization when DOM is loaded
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    inicializar();
}

// Export functions for global use
window.VittaeApp = {
    showPage,
    toggleMobileMenu,
    closeMobileMenu,
    showToast,
    validateForm,
    saveToStorage,
    loadFromStorage,
    isMobile,
    isTablet
};

const checkboxes = document.querySelectorAll('.pill input[type="checkbox"], .pill input[type="radio"]');
checkboxes.forEach(checkbox => {
    checkbox.addEventListener('change', (e) => {
        const pill = e.target.closest('.pill');
        if (e.target.type === 'radio') {
            // Uncheck other radios in the same group
            const name = e.target.name;
            const groupRadios = document.querySelectorAll(`.pill input[type="radio"][name="${name}"]`);
            groupRadios.forEach(radio => {
                const radioPill = radio.closest('.pill');
                if (radio !== e.target) {
                    radioPill.classList.remove('active');
                }
            });
        }
        // Toggle active class based on checked state
        if (e.target.checked) {
            pill.classList.add('active');
        } else {
            pill.classList.remove('active');
        }
    });
});
// Prevent label text selection on rapid clicks
const pills = document.querySelectorAll('.pill');
pills.forEach(pill => {
    pill.addEventListener('mousedown', (e) => {
        e.preventDefault();
    });
}); 