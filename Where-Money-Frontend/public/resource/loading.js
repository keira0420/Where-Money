/**
 * 初始化加载效果的svg格式logo
 * @param { string }id - 元素id
 */
function initSvgLogo(id) {
    const isDesktop = window.matchMedia ? window.matchMedia("(min-width: 1024px)").matches : true;
    const desktopDisplay = isDesktop ? "block" : "none";
    const mobileDisplay = isDesktop ? "none" : "block";
    const svgStr = `<svg width="800" height="600" viewBox="0 0 800 600" xmlns="http://www.w3.org/2000/svg" x="0px" y="0px">
    <g>
        <title>Layer 1</title>
        <g class="loading-title-desktop" style="display:${desktopDisplay}">
            <text style="fill:currentColor" x="150" y="325" id="svg_1" font-size="70" font-family="'Assistant'" xml:space="preserve" font-style="italic" font-weight="bold">Where Money</text>
            <path xmlns="http://www.w3.org/2000/svg" stroke="#14b8a6" stroke-width="1%" id="svg_4" d="m150,341.90909l119,0" opacity="undefined" fill="#000" />
        </g>
        <g class="loading-title-mobile" style="display:${mobileDisplay}">
            <text style="fill:currentColor" font-size="70" font-family="'Assistant'" font-style="italic" font-weight="bold" text-anchor="middle" x="400" y="285">Where</text>
            <text style="fill:currentColor" font-size="70" font-family="'Assistant'" font-style="italic" font-weight="bold" text-anchor="middle" x="400" y="365">Money</text>
            <line x1="340" x2="460" y1="301.90909" y2="301.90909" stroke="#14b8a6" stroke-width="1%" />
        </g>
    </g>
</svg>
`;
    const appEl = document.querySelector(id);
    const div = document.createElement("div");
    div.innerHTML = svgStr;
    if (appEl) {
        appEl.appendChild(div);
    }
}

function addThemeColorCssVars() {
    const key = "__THEME_COLOR__";
    const themeColor = window.localStorage.getItem(key) || "#2092C6";
    document.documentElement.style.cssText = `--primary-color: ${themeColor}`;
}

initSvgLogo("#loadingLogo");

addThemeColorCssVars();
