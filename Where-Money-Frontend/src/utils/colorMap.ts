const percentColorsForProgress = [
    {
        pct: 0,
        color: {r: 0xff, g: 0x00, b: 0}
    },
    {
        pct: 50,
        color: {r: 0xff, g: 0xff, b: 0}
    },
    {
        pct: 100,
        color: {r: 0x00, g: 0x80, b: 0}
    }
];

const percentColorsForText = [
    {
        pct: 0,
        color: {r: 0xff, g: 0x00, b: 0}
    },
    {
        pct: 50,
        color: {r: 0xea, g: 0xcd, b: 0x76}
    },
    {
        pct: 100,
        color: {r: 0x05, g: 0x77, b: 0x48}
    }
];

export const getColor = function (pct: number, type: string = "progress", begin: number = 0, end: number = 100) {
    let percentColors: any[];
    if (type == "progress") {
        percentColors = percentColorsForProgress;
    } else {
        percentColors = percentColorsForText;
    }
    percentColorsForProgress[0].pct = begin;
    percentColorsForProgress[1].pct = (begin + end) / 2;
    percentColorsForProgress[2].pct = end;
    percentColorsForText[0].pct = begin;
    percentColorsForText[1].pct = (begin + end) / 2;
    percentColorsForText[2].pct = end;
    let i = 1;
    for (; i < percentColors.length - 1; i++) {
        if (pct < percentColors[i].pct) {
            break;
        }
    }
    const lower = percentColors[i - 1];
    const upper = percentColors[i];
    const range = upper.pct - lower.pct;
    const rangePct = (pct - lower.pct) / range;
    const pctLower = 1 - rangePct;
    const pctUpper = rangePct;
    const color = {
        r: Math.floor(lower.color.r * pctLower + upper.color.r * pctUpper),
        g: Math.floor(lower.color.g * pctLower + upper.color.g * pctUpper),
        b: Math.floor(lower.color.b * pctLower + upper.color.b * pctUpper)
    };
    return `rgb(${[color.r, color.g, color.b].join(",")})`;
};

const rgb2Hex = (rgb: Array<number>) => {
    let r = rgb[0];
    let g = rgb[1];
    let b = rgb[2];
    return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
};

function getLightColor(oldRgb: Array<number>, level: number) {
    let rgb = [...oldRgb];
    for (let i = 0; i < 3; i++) {
        rgb[i] = Math.floor((255 - rgb[i]) * level + rgb[i]);
    }
    return [rgb[0], rgb[1], rgb[2]];
}

function getDarkColor(oldRgb: Array<number>, level: number) {
    let rgb = [...oldRgb];
    for (let i = 0; i < 3; i++) {
        rgb[i] = Math.floor(rgb[i] * (1 - level));
    }
    return [rgb[0], rgb[1], rgb[2]];
}
