/**
 * CofiCall - Landing Page Interactivity
 * Handle mobile navigation drawer, interactive screenshots carousel, and animations.
 */

document.addEventListener('DOMContentLoaded', () => {
    initMobileMenu();
    initCarousel();
    initHeroAnimation();
});

/**
 * Mobile Drawer Menu functionality
 */
function initMobileMenu() {
    const mobileMenuBtn = document.querySelector('.mobile-menu-btn');
    const mobileDrawer = document.querySelector('.mobile-drawer');
    const drawerLinks = document.querySelectorAll('.mobile-drawer a');
    
    if (!mobileMenuBtn || !mobileDrawer) return;

    // Toggle menu
    mobileMenuBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isActive = mobileDrawer.classList.toggle('active');
        
        // Update menu icon (FaBars -> FaXmark)
        const icon = mobileMenuBtn.querySelector('i');
        if (icon) {
            if (isActive) {
                icon.className = 'fa-solid fa-xmark';
                mobileMenuBtn.setAttribute('aria-expanded', 'true');
            } else {
                icon.className = 'fa-solid fa-bars';
                mobileMenuBtn.setAttribute('aria-expanded', 'false');
            }
        }
    });

    // Close drawer when a link is clicked
    drawerLinks.forEach(link => {
        link.addEventListener('click', () => {
            mobileDrawer.classList.remove('active');
            const icon = mobileMenuBtn.querySelector('i');
            if (icon) icon.className = 'fa-solid fa-bars';
            mobileMenuBtn.setAttribute('aria-expanded', 'false');
        });
    });

    // Close drawer when clicking outside
    document.addEventListener('click', (e) => {
        if (mobileDrawer.classList.contains('active') && 
            !mobileDrawer.contains(e.target) && 
            !mobileMenuBtn.contains(e.target)) {
            mobileDrawer.classList.remove('active');
            const icon = mobileMenuBtn.querySelector('i');
            if (icon) icon.className = 'fa-solid fa-bars';
            mobileMenuBtn.setAttribute('aria-expanded', 'false');
        }
    });
}

/**
 * Screenshots Carousel Component
 */
function initCarousel() {
    const track = document.querySelector('.carousel-track');
    const slides = Array.from(document.querySelectorAll('.carousel-slide'));
    const prevBtn = document.querySelector('.prev-btn');
    const nextBtn = document.querySelector('.next-btn');
    const dotsContainer = document.querySelector('.carousel-dots');

    if (!track || slides.length === 0 || !prevBtn || !nextBtn || !dotsContainer) return;

    let currentIndex = 0;
    let slidesVisible = getSlidesVisible();
    let maxIndex = Math.max(0, slides.length - slidesVisible);
    let autoplayTimer = null;

    // Initialize dots
    setupDots();
    updateCarouselPosition();

    // Event Listeners for Buttons
    prevBtn.addEventListener('click', () => {
        resetAutoplay();
        navigateCarousel(currentIndex - 1);
    });

    nextBtn.addEventListener('click', () => {
        resetAutoplay();
        navigateCarousel(currentIndex + 1);
    });

    // Handle Window Resize
    window.addEventListener('resize', () => {
        const newSlidesVisible = getSlidesVisible();
        if (newSlidesVisible !== slidesVisible) {
            slidesVisible = newSlidesVisible;
            maxIndex = Math.max(0, slides.length - slidesVisible);
            if (currentIndex > maxIndex) {
                currentIndex = maxIndex;
            }
            setupDots();
        }
        updateCarouselPosition();
    });

    // Start Autoplay
    startAutoplay();

    // Pause autoplay on mouse enter / resume on leave
    const wrapper = document.querySelector('.carousel-wrapper');
    if (wrapper) {
        wrapper.addEventListener('mouseenter', () => clearInterval(autoplayTimer));
        wrapper.addEventListener('mouseleave', startAutoplay);
    }

    /**
     * Determine how many slides should be visible based on viewport width
     */
    function getSlidesVisible() {
        const width = window.innerWidth;
        if (width <= 480) return 1;
        if (width <= 768) return 2;
        return 3;
    }

    /**
     * Create pagination dots dynamically
     */
    function setupDots() {
        dotsContainer.innerHTML = '';
        const dotsCount = maxIndex + 1;

        if (dotsCount <= 1) {
            dotsContainer.style.display = 'none';
            return;
        } else {
            dotsContainer.style.display = 'flex';
        }

        for (let i = 0; i < dotsCount; i++) {
            const dot = document.createElement('div');
            dot.className = `carousel-dot ${i === currentIndex ? 'active' : ''}`;
            dot.addEventListener('click', () => {
                resetAutoplay();
                navigateCarousel(i);
            });
            dotsContainer.appendChild(dot);
        }
    }

    /**
     * Navigate to specific index
     */
    function navigateCarousel(index) {
        if (index < 0) {
            currentIndex = maxIndex; // Loop to end
        } else if (index > maxIndex) {
            currentIndex = 0; // Loop to beginning
        } else {
            currentIndex = index;
        }
        updateCarouselPosition();
    }

    /**
     * Translate the track and update dots active state
     */
    function updateCarouselPosition() {
        if (slides.length === 0) return;

        // Calculate translation offset based on real elements
        let offset = 0;
        if (currentIndex > 0 && slides.length > 1) {
            const firstSlideRect = slides[0].getBoundingClientRect();
            const secondSlideRect = slides[1].getBoundingClientRect();
            // Distance is the delta between the left positions of first two slides
            const distance = secondSlideRect.left - firstSlideRect.left;
            offset = currentIndex * distance;
        }

        track.style.transform = `translateX(-${offset}px)`;

        // Update Dots
        const dots = Array.from(dotsContainer.querySelectorAll('.carousel-dot'));
        dots.forEach((dot, idx) => {
            if (idx === currentIndex) {
                dot.classList.add('active');
            } else {
                dot.classList.remove('active');
            }
        });

        // Toggle button states (optional: disable instead of looping, but looping is preferred here)
        // prevBtn.style.opacity = currentIndex === 0 ? '0.5' : '1';
        // nextBtn.style.opacity = currentIndex === maxIndex ? '0.5' : '1';
    }

    /**
     * Autoplay methods
     */
    function startAutoplay() {
        clearInterval(autoplayTimer);
        autoplayTimer = setInterval(() => {
            navigateCarousel(currentIndex + 1);
        }, 5000); // Shift every 5 seconds
    }

    function resetAutoplay() {
        clearInterval(autoplayTimer);
        startAutoplay();
    }
}

/**
 * Micro-animations and image transitions for Hero mockup
 */
function initHeroAnimation() {
    const heroImage = document.getElementById('mockup-screen');
    if (!heroImage) return;

    // List of screens to rotate inside the hero mockup
    const screens = [
        'assets/screen-home.png',
        'assets/screen-directory-dark.png',
        'assets/screen-settings.png'
    ];
    
    let screenIndex = 0;

    // Prefetch images to avoid blank flashes
    screens.forEach(src => {
        const img = new Image();
        img.src = src;
    });

    setInterval(() => {
        screenIndex = (screenIndex + 1) % screens.length;
        
        // Premium transition effect (fade out, change source, fade in)
        heroImage.style.opacity = '0';
        heroImage.style.transition = 'opacity 0.4s ease-in-out';
        
        setTimeout(() => {
            heroImage.src = screens[screenIndex];
            heroImage.onload = () => {
                heroImage.style.opacity = '1';
            };
        }, 400);

    }, 7000); // Rotates every 7 seconds
}
