const decimalFormatter = new Intl.NumberFormat("zh-CN", {
    style: "decimal",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
});

const currencyFormatter = new Intl.NumberFormat("zh-CN", {
    style: "currency",
    currency: "CNY"
});

function formattedCurrencyNoSymbol(currency?: number) {
    if (currency === undefined) return 0;
    return decimalFormatter.format(currency);
}

function formattedCurrencyWithSymbol(currency?: number) {
    if (currency === undefined) return 0;
    return currencyFormatter.format(currency);
}

export {formattedCurrencyNoSymbol, formattedCurrencyWithSymbol};
