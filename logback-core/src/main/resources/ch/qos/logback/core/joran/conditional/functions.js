var OptionHelper;
if (typeof(Java) !== "undefined") {
    // Nashorn interpreter
    OptionHelper = Java.type("ch.qos.logback.core.util.OptionHelper");
} else if (typeof(Packages) !== "undefined") {
    // Rhino interpreter
    OptionHelper = Packages.ch.qos.logback.core.util.OptionHelper;
    String.prototype.contains = function(str) {
        return this.indexOf(str) >= 0;
    };
} else {
    throw "cannot access OptionHelper (neither Nashorn nor Rhino?)";
}

var property = function(key) {
    var val = OptionHelper.propertyLookup(key, props, ctx);
    return val ? val : "";
};
var p = property;

var isDefined = function(key) {
    return !(p(key) === "");
};

var isNull = function(key) {
    return p(key) === "";
};
