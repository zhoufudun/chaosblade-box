"use strict";
(self["webpackChunk"] = self["webpackChunk"] || []).push([[144],{

/***/ 93484:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
var next_1 = __webpack_require__(30156);
var withThemeClass_1 = __webpack_require__(46949);
var react_1 = __importDefault(__webpack_require__(27378));
var useCssVar_1 = __webpack_require__(67056);
var Pagination = (function (props) {
    var theme = useCssVar_1.useCssVar('--alicloudfe-components-theme').trim();
    // 混合云云效主题默认不显示下一页
    var shape = (function () {
        if (theme.startsWith('hybridcloud') || theme.startsWith('yunxiao')) {
            return 'arrow-only';
        }
        return 'normal';
    })();
    return react_1.default.createElement(next_1.Pagination, __assign({ shape: shape }, props));
});
exports["default"] = withThemeClass_1.withThemeClass(Pagination);


/***/ }),

/***/ 94188:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __spreadArrays = (this && this.__spreadArrays) || function () {
    for (var s = 0, i = 0, il = arguments.length; i < il; i++) s += arguments[i].length;
    for (var r = Array(s), k = 0, i = 0; i < il; i++)
        for (var a = arguments[i], j = 0, jl = a.length; j < jl; j++, k++)
            r[k] = a[j];
    return r;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
var next_1 = __webpack_require__(30156);
var react_1 = __importStar(__webpack_require__(27378));
var classnames_1 = __importDefault(__webpack_require__(60042));
var popupHoc_1 = __importStar(__webpack_require__(1073));
var Search = react_1.default.forwardRef(function (props, ref) {
    var _a = react_1.useState(false), focus = _a[0], setFocus = _a[1];
    var _b = react_1.useState(false), visible = _b[0], setVisible = _b[1];
    var onFocus = react_1.useCallback(function (e) {
        setFocus(true);
        if (typeof props.onFocus === 'function') {
            props.onFocus(e);
        }
    }, [props.onFocus]);
    var onBlur = react_1.useCallback(function (e) {
        setFocus(false);
        if (typeof props.onBlur === 'function') {
            props.onBlur(e);
        }
    }, [props.onBlur]);
    var onVisibleChange = react_1.useCallback(function (v) {
        var rest = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            rest[_i - 1] = arguments[_i];
        }
        setVisible(v);
        if (typeof props.onVisibleChange === 'function') {
            // @ts-ignore
            props.onVisibleChange.apply(props, __spreadArrays([v], rest));
        }
    }, [props.onVisibleChange]);
    // Search filter也是个选择器，要设置它的弹层offset
    var defaultOffsetY = popupHoc_1.useDefaultOffsetY();
    var filterProps = react_1.useMemo(function () {
        var _a;
        var popupProps = __assign({ align: 'tl bl', offset: [0, defaultOffsetY] }, (_a = props.filterProps) === null || _a === void 0 ? void 0 : _a.popupProps);
        var filterProps = __assign(__assign({}, props.filterProps), { popupProps: popupProps });
        return filterProps;
    }, [defaultOffsetY, props.filterProps]);
    return (react_1.default.createElement(next_1.Search, __assign({}, props, { ref: ref, onFocus: onFocus, onBlur: onBlur, onVisibleChange: onVisibleChange, className: classnames_1.default(props.className, 
        // 根据当前状态增加类名，用来做样式覆盖
        props.searchText ? 'custom-search-text' : null, focus ? 'focusing' : false, visible ? 'visible' : false, props.disabled ? 'disabled' : false, props.searchText ? null : 'next-search-no-custom-search-text'), filterProps: filterProps })));
});
exports["default"] = popupHoc_1.default(Search);


/***/ }),

/***/ 16664:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
var react_1 = __importDefault(__webpack_require__(27378));
var prop_types_1 = __importDefault(__webpack_require__(23615));
var classnames_1 = __importDefault(__webpack_require__(60042));
var next_1 = __webpack_require__(30156);
var constants_1 = __webpack_require__(66693);
var ColoredTag = function (_a) {
    var type = _a.type, className = _a.className, restProps = __rest(_a, ["type", "className"]);
    return (react_1.default.createElement(next_1.Tag, __assign({}, restProps, { className: classnames_1.default(constants_1.COLORED_CLASS_NAME, constants_1.COLORED_CLASS_NAME + "-" + type, className) })));
};
/**
 * Prop types
 * @static
 * @type {Object}
 */
ColoredTag.propTypes = __assign(__assign({}, next_1.Tag.propTypes), { 
    // Specific color of tag
    type: prop_types_1.default.oneOf(Object.values(constants_1.Color)), 
    // Class name of tag
    className: prop_types_1.default.string });
/**
 * Default props
 * @static
 * @type {Object}
 */
ColoredTag.defaultProps = {
    type: constants_1.Color.LIGHT_STEEL_BLUE
};
ColoredTag[constants_1.PROTECTED_TYPE] = 'Tag';
exports["default"] = ColoredTag;


/***/ }),

/***/ 79148:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    Object.defineProperty(o, k2, { enumerable: true, get: function() { return m[k]; } });
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
var react_1 = __importStar(__webpack_require__(27378));
var prop_types_1 = __importDefault(__webpack_require__(23615));
var classnames_1 = __importDefault(__webpack_require__(60042));
var next_1 = __webpack_require__(30156);
var Colored_1 = __importDefault(__webpack_require__(16664));
var constants_1 = __webpack_require__(66693);
var TagGroup = next_1.Tag.Group;
var defaultAvaliableColors = [
    constants_1.Color.LIGHT_STEEL_BLUE,
    constants_1.Color.PLUM,
    constants_1.Color.MISTY_ROSE,
    constants_1.Color.LIGHT_GOLDENROD_YELLOW,
    constants_1.Color.PALE_GREEN
];
var ColoredGroup = function (_a) {
    var className = _a.className, style = _a.style, _b = _a.avaliableColors, avaliableColors = _b === void 0 ? defaultAvaliableColors : _b, children = _a.children;
    return (react_1.default.createElement(TagGroup, { className: classnames_1.default(constants_1.COLORED_GROUP_CLASS_NAME, className), style: style }, react_1.Children.map(children, function (elem, i) {
        var hijackedElem = elem;
        try {
            var protectedElemType = elem.type[constants_1.PROTECTED_TYPE];
            if (protectedElemType === 'Tag') {
                hijackedElem = (react_1.default.createElement(Colored_1.default, __assign({}, elem.props, { type: avaliableColors[i % 5] })));
            }
        }
        catch (err) {
            /** DO NOT thrown unexpected error but swallow it */
        }
        return hijackedElem;
    })));
};
ColoredGroup.propTypes = {
    className: prop_types_1.default.string,
    style: prop_types_1.default.objectOf(prop_types_1.default.any),
    avaliableColors: prop_types_1.default.arrayOf(prop_types_1.default.string),
    children: prop_types_1.default.node
};
exports["default"] = ColoredGroup;


/***/ }),

/***/ 66693:
/***/ ((__unused_webpack_module, exports) => {


Object.defineProperty(exports, "__esModule", ({ value: true }));
exports.PROTECTED_TYPE = exports.COLORED_GROUP_CLASS_NAME = exports.COLORED_CLASS_NAME = exports.Color = void 0;
/**
 * Allowed colors of tag
 * @enum {String}
 */
exports.Color = {
    LIGHT_STEEL_BLUE: 'light-steel-blue',
    PLUM: 'plum',
    MISTY_ROSE: 'misty-rose',
    LIGHT_GOLDENROD_YELLOW: 'light-goldenrod-yellow',
    PALE_GREEN: 'pale-green',
    SILVER: 'silver',
    GRAY: 'gray',
};
/**
 * @const
 * @type {String}
 */
exports.COLORED_CLASS_NAME = 'wind-tag-colored';
/**
 * @const
 * @type {String}
 */
exports.COLORED_GROUP_CLASS_NAME = exports.COLORED_CLASS_NAME + "-group";
/**
 * 标记
 */
exports.PROTECTED_TYPE = '__WIND_TAG_';


/***/ }),

/***/ 51834:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
exports.wrap = void 0;
var constants_1 = __webpack_require__(66693);
var Colored_1 = __importDefault(__webpack_require__(16664));
var ColoredGroup_1 = __importDefault(__webpack_require__(79148));
/**
 * 兼容旧版wind的API
 */
function wrap(Tag) {
    ;
    Tag.Colored = Colored_1.default;
    Tag.ColoredGroup = ColoredGroup_1.default;
    Tag[constants_1.PROTECTED_TYPE] = 'Tag';
    return Tag;
}
exports.wrap = wrap;


/***/ }),

/***/ 36939:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {


var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
var __rest = (this && this.__rest) || function (s, e) {
    var t = {};
    for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p) && e.indexOf(p) < 0)
        t[p] = s[p];
    if (s != null && typeof Object.getOwnPropertySymbols === "function")
        for (var i = 0, p = Object.getOwnPropertySymbols(s); i < p.length; i++) {
            if (e.indexOf(p[i]) < 0 && Object.prototype.propertyIsEnumerable.call(s, p[i]))
                t[p[i]] = s[p[i]];
        }
    return t;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", ({ value: true }));
var next_1 = __webpack_require__(30156);
var react_1 = __importDefault(__webpack_require__(27378));
var withThemeClass_1 = __webpack_require__(46949);
var hoist_non_react_statics_1 = __importDefault(__webpack_require__(55839));
var WindColorfulTag_1 = __webpack_require__(51834);
var useCssVar_1 = __webpack_require__(67056);
var classnames_1 = __importDefault(__webpack_require__(60042));
var Tag = WindColorfulTag_1.wrap(withThemeClass_1.withThemeClass(react_1.default.forwardRef(function (props, ref) {
    var _a;
    var children = props.children, color = props.color, _b = props.prefix, prefix = _b === void 0 ? 'next-' : _b;
    var className = props.className, others = __rest(props, ["className"]);
    var theme = useCssVar_1.useCssVar('--alicloudfe-components-theme').trim();
    if (theme === 'hybridcloud' ||
        theme === 'hybridcloud-dark' ||
        theme === 'yunxiao' ||
        theme === 'yunxiao-dark') {
        return (react_1.default.createElement(next_1.Tag, __assign({ ref: ref, className: classnames_1.default((_a = {}, _a[prefix + "tag-custom-" + color] = true, _a), className) }, others), children));
    }
    return (react_1.default.createElement(next_1.Tag, __assign({}, props, { ref: ref }), children));
})));
hoist_non_react_statics_1.default(Tag, next_1.Tag);
// @ts-ignore
Tag.displayName = next_1.Tag.displayName;
exports["default"] = Tag;


/***/ })

}]);
//# sourceMappingURL=144.bundle.js.map