(function () {
  try {
    function postY() {
      var y = window.pageYOffset || document.documentElement.scrollTop || document.body.scrollTop || 0;
      if (window.ReactNativeWebView) {
        window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'scroll', y: y }));
      }
    }
    var timer;
    function onScroll() {
      clearTimeout(timer);
      timer = setTimeout(postY, 200);
    }
    window.addEventListener('scroll', onScroll, { passive: true });

    var startX = 0, startY = 0, startT = 0;
    var TAP_MAX_DELTA = 10;
    var TAP_MAX_TIME = 400;
    var SWIPE_MIN_DX = 50;
    var SWIPE_MAX_DY = 35;
    var SWIPE_MAX_TIME = 600;
    var INTERACTIVE_SELECTOR = 'a,button,input,select,textarea,label,[role="link"],[role="button"]';

    function postPage(dir) {
      if (window.ReactNativeWebView) {
        window.ReactNativeWebView.postMessage(JSON.stringify({ t: 'page', dir: dir }));
      }
    }
    function onTouchStart(e) {
      if (!e.changedTouches || e.changedTouches.length !== 1) {
        startT = 0;
        return;
      }
      var p = e.changedTouches[0];
      startX = p.clientX; startY = p.clientY; startT = Date.now();
    }
    function onTouchEnd(e) {
      if (!startT || !e.changedTouches || e.changedTouches.length !== 1) {
        startT = 0;
        return;
      }
      var p = e.changedTouches[0];
      var dx = p.clientX - startX;
      var dy = p.clientY - startY;
      var dt = Date.now() - startT;
      startT = 0;
      if (window.getSelection && String(window.getSelection()).length > 0) return;
      if (dt <= SWIPE_MAX_TIME && Math.abs(dx) >= SWIPE_MIN_DX && Math.abs(dy) <= SWIPE_MAX_DY) {
        postPage(dx < 0 ? 'next' : 'prev');
        return;
      }
      if (dt <= TAP_MAX_TIME && Math.abs(dx) <= TAP_MAX_DELTA && Math.abs(dy) <= TAP_MAX_DELTA) {
        if (e.target && e.target.closest && e.target.closest(INTERACTIVE_SELECTOR)) return;
        var w = window.innerWidth || document.documentElement.clientWidth || 1;
        var frac = p.clientX / w;
        if (frac <= 0.33) postPage('prev');
        else if (frac >= 0.67) postPage('next');
      }
    }
    window.addEventListener('touchstart', onTouchStart, { passive: true });
    window.addEventListener('touchend', onTouchEnd, { passive: true });
    window.addEventListener('touchcancel', function () { startT = 0; }, { passive: true });
  } catch (e) {}
  true;
})();
