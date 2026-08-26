// ===== QR Pro — التفاعلات =====

(function () {
  "use strict";

  // شريط التنقل العلوي عند التمرير
  const navbar = document.getElementById("navbar");
  function onScroll() {
    if (navbar) navbar.classList.toggle("scrolled", window.scrollY > 24);
  }
  window.addEventListener("scroll", onScroll, { passive: true });
  onScroll();

  // تمرير ناعم مع مراعاة ارتفاع شريط التنقل
  document.querySelectorAll('a[href^="#"]').forEach(function (anchor) {
    anchor.addEventListener("click", function (e) {
      const id = anchor.getAttribute("href").slice(1);
      const target = document.getElementById(id);
      if (!target) return;
      e.preventDefault();
      const offset = (navbar ? navbar.offsetHeight : 0) + 12;
      window.scrollTo({
        top: target.getBoundingClientRect().top + window.scrollY - offset,
        behavior: "smooth",
      });
    });
  });

  // أنيميشن الظهور عند التمرير
  const revealObserver = new IntersectionObserver(
    function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add("visible");
          revealObserver.unobserve(entry.target);
        }
      });
    },
    { threshold: 0.14, rootMargin: "0px 0px -30px 0px" }
  );
  document.querySelectorAll(".reveal").forEach(function (el) {
    revealObserver.observe(el);
  });

  // لمعان الزجاج يتبع مؤشر الفأرة
  document.querySelectorAll(".sheen").forEach(function (card) {
    card.addEventListener("pointermove", function (e) {
      const rect = card.getBoundingClientRect();
      card.style.setProperty("--mx", (e.clientX - rect.left) + "px");
      card.style.setProperty("--my", (e.clientY - rect.top) + "px");
    });
  });

  // فتح سؤال واحد فقط في الأسئلة الشائعة
  const faqItems = document.querySelectorAll(".faq-item");
  faqItems.forEach(function (item) {
    item.addEventListener("toggle", function () {
      if (!item.open) return;
      faqItems.forEach(function (other) {
        if (other !== item) other.open = false;
      });
    });
  });

  // شريط التنقل السفلي: تمييز القسم النشط تلقائيًا
  const tabs = Array.prototype.slice.call(document.querySelectorAll(".tabbar .tab"));
  if (tabs.length) {
    const sectionObserver = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (!entry.isIntersecting) return;
          tabs.forEach(function (tab) {
            tab.classList.toggle(
              "active",
              tab.getAttribute("href") === "#" + entry.target.id
            );
          });
        });
      },
      { rootMargin: "-40% 0px -55% 0px", threshold: 0 }
    );

    tabs.forEach(function (tab) {
      const id = tab.getAttribute("href").slice(1);
      const section = document.getElementById(id);
      if (section) sectionObserver.observe(section);

      tab.addEventListener("click", function () {
        tabs.forEach(function (t) { t.classList.remove("active"); });
        tab.classList.add("active");
      });
    });
  }

  // عداد أرقام شريط الثقة
  const counters = document.querySelectorAll(".trust-item b");
  const counterObserver = new IntersectionObserver(
    function (entries) {
      entries.forEach(function (entry) {
        if (!entry.isIntersecting) return;
        animateCounter(entry.target);
        counterObserver.unobserve(entry.target);
      });
    },
    { threshold: 0.6 }
  );
  counters.forEach(function (el) { counterObserver.observe(el); });

  function animateCounter(el) {
    const raw = el.textContent.trim();
    const match = raw.match(/^([\d.,]+)(.*)$/);
    if (!match || raw.includes("<") || raw.includes("+")) return;

    const end = parseFloat(match[1].replace(/,/g, ""));
    if (isNaN(end)) return;
    const suffix = match[2];
    const decimals = raw.includes(".") ? raw.split(".")[1].replace(/[^\d]/g, "").length : 0;
    const duration = 1300;
    const startTime = performance.now();

    function tick(now) {
      const progress = Math.min((now - startTime) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const value = end * eased;
      el.textContent =
        (decimals ? value.toFixed(decimals) : Math.round(value).toLocaleString("en-US")) + suffix;
      if (progress < 1) requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  }
})();
