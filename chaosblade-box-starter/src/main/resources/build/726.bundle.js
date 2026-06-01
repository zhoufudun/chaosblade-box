(self["webpackChunk"] = self["webpackChunk"] || []).push([[726],{

/***/ 42499:
/***/ (function(__unused_webpack_module, exports, __webpack_require__) {

"use strict";

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
var next_1 = __webpack_require__(30156);
var classnames_1 = __importDefault(__webpack_require__(60042));
var hoist_non_react_statics_1 = __importDefault(__webpack_require__(55839));
var useCssVar_1 = __webpack_require__(67056);
var Table = function (_a) {
    var _b;
    var hasBorder = _a.hasBorder, rowSelection = _a.rowSelection, className = _a.className, restProps = __rest(_a, ["hasBorder", "rowSelection", "className"]);
    var theme = useCssVar_1.useCssVar('--alicloudfe-components-theme');
    var isWind = theme.trim() === 'wind';
    if (hasBorder === undefined) {
        // wind主题的默认值为true
        hasBorder = isWind;
    }
    return (react_1.default.createElement(next_1.Table, __assign({ hasBorder: hasBorder, rowSelection: rowSelection, className: classnames_1.default(className, (_b = {},
            // 对于有选择列的表格，我们增加一个类名，方便对这种表格进行样式覆盖
            _b['with-row-select'] = !!rowSelection,
            _b['is-wind'] = isWind,
            _b)) }, restProps)));
};
hoist_non_react_statics_1.default(Table, next_1.Table);
var exported = Table;
exports["default"] = exported;


/***/ }),

/***/ 77726:
/***/ (function(module, exports, __webpack_require__) {

var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;var _typeof = __webpack_require__(24596);

var _Object$defineProperty = __webpack_require__(67394);

var _WeakMap = __webpack_require__(93168);

var _Object$getOwnPropertyDescriptor = __webpack_require__(23587);

var _Object$keys = __webpack_require__(83452);

var _Object$getOwnPropertySymbols = __webpack_require__(95315);

var _Object$getOwnPropertyDescriptors = __webpack_require__(63774);

var _Object$defineProperties = __webpack_require__(92937);

(function (global, factory) {
  if (true) {
    !(__WEBPACK_AMD_DEFINE_ARRAY__ = [exports, __webpack_require__(17534), __webpack_require__(42499), __webpack_require__(17225), __webpack_require__(72153), __webpack_require__(77809), __webpack_require__(57379), __webpack_require__(81853), __webpack_require__(27378), __webpack_require__(66697), __webpack_require__(14798), __webpack_require__(68055), __webpack_require__(5282), __webpack_require__(99328), __webpack_require__(14870), __webpack_require__(73358)], __WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
		__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
		(__WEBPACK_AMD_DEFINE_FACTORY__.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__)) : __WEBPACK_AMD_DEFINE_FACTORY__),
		__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
  } else { var mod; }
})(typeof globalThis !== "undefined" ? globalThis : typeof self !== "undefined" ? self : this, function (_exports, _message, _table, _icon, _button, _asyncToGenerator2, _defineProperty2, _slicedToArray2, _react, _Translation, _i18n, _locale, _constants, _sreUtils, _sreUtilsDva, _Manage) {
  "use strict";

  var _interopRequireDefault = __webpack_require__(67971);

  _Object$defineProperty(_exports, "__esModule", {
    value: true
  });

  _exports["default"] = Tools;
  _message = _interopRequireDefault(_message);
  _table = _interopRequireDefault(_table);
  _icon = _interopRequireDefault(_icon);
  _button = _interopRequireDefault(_button);
  _asyncToGenerator2 = _interopRequireDefault(_asyncToGenerator2);
  _defineProperty2 = _interopRequireDefault(_defineProperty2);
  _slicedToArray2 = _interopRequireDefault(_slicedToArray2);
  _react = _interopRequireWildcard(_react);
  _Translation = _interopRequireDefault(_Translation);
  _i18n = _interopRequireDefault(_i18n);
  _locale = _interopRequireDefault(_locale);

  function _getRequireWildcardCache(nodeInterop) { if (typeof _WeakMap !== "function") return null; var cacheBabelInterop = new _WeakMap(); var cacheNodeInterop = new _WeakMap(); return (_getRequireWildcardCache = function _getRequireWildcardCache(nodeInterop) { return nodeInterop ? cacheNodeInterop : cacheBabelInterop; })(nodeInterop); }

  function _interopRequireWildcard(obj, nodeInterop) { if (!nodeInterop && obj && obj.__esModule) { return obj; } if (obj === null || _typeof(obj) !== "object" && typeof obj !== "function") { return { "default": obj }; } var cache = _getRequireWildcardCache(nodeInterop); if (cache && cache.has(obj)) { return cache.get(obj); } var newObj = {}; var hasPropertyDescriptor = _Object$defineProperty && _Object$getOwnPropertyDescriptor; for (var key in obj) { if (key !== "default" && Object.prototype.hasOwnProperty.call(obj, key)) { var desc = hasPropertyDescriptor ? _Object$getOwnPropertyDescriptor(obj, key) : null; if (desc && (desc.get || desc.set)) { _Object$defineProperty(newObj, key, desc); } else { newObj[key] = obj[key]; } } } newObj["default"] = obj; if (cache) { cache.set(obj, newObj); } return newObj; }

  function ownKeys(object, enumerableOnly) { var keys = _Object$keys(object); if (_Object$getOwnPropertySymbols) { var symbols = _Object$getOwnPropertySymbols(object); enumerableOnly && (symbols = symbols.filter(function (sym) { return _Object$getOwnPropertyDescriptor(object, sym).enumerable; })), keys.push.apply(keys, symbols); } return keys; }

  function _objectSpread(target) { for (var i = 1; i < arguments.length; i++) { var source = null != arguments[i] ? arguments[i] : {}; i % 2 ? ownKeys(Object(source), !0).forEach(function (key) { (0, _defineProperty2["default"])(target, key, source[key]); }) : _Object$getOwnPropertyDescriptors ? _Object$defineProperties(target, _Object$getOwnPropertyDescriptors(source)) : ownKeys(Object(source)).forEach(function (key) { _Object$defineProperty(target, key, _Object$getOwnPropertyDescriptor(source, key)); }); } return target; }

  function Tools() {
    var dispatch = (0, _sreUtilsDva.useDispatch)();
    var parsed = (0, _sreUtils.parseQuery)();

    var _useState = (0, _react.useState)([]),
        _useState2 = (0, _slicedToArray2["default"])(_useState, 2),
        dataSource = _useState2[0],
        setDataSource = _useState2[1];

    var _useState3 = (0, _react.useState)(null),
        _useState4 = (0, _slicedToArray2["default"])(_useState3, 2),
        actionData = _useState4[0],
        setActionData = _useState4[1];

    var _useState5 = (0, _react.useState)(false),
        _useState6 = (0, _slicedToArray2["default"])(_useState5, 2),
        isUpdate = _useState6[0],
        setIsUpdate = _useState6[1];

    var _useState7 = (0, _react.useState)(false),
        _useState8 = (0, _slicedToArray2["default"])(_useState7, 2),
        loadingInstall = _useState8[0],
        setLoadingInstall = _useState8[1];

    var id = parsed.id,
        mode = parsed.mode;
    (0, _react.useEffect)(function () {
      dispatch.pageHeader.setTitle(_i18n["default"].t('Tool management').toString());
      dispatch.pageHeader.showBackArrow(true);
      dispatch.pageHeader.setBreadCrumbItems([_constants.DEFAULT_BREADCRUMB_ITEM].concat([_objectSpread({}, _Manage.breadCrumbConf[/\agentmanage\//.test(location.pathname) ? 'ahaos' : 'manage']), {
        key: 'setting/tools',
        value: _i18n["default"].t('Tool management').toString(),
        path: location.pathname
      }]));
    }, []);
    (0, _react.useEffect)(function () {
      (0, _asyncToGenerator2["default"])( /*#__PURE__*/regeneratorRuntime.mark(function _callee() {
        return regeneratorRuntime.wrap(function _callee$(_context) {
          while (1) {
            switch (_context.prev = _context.next) {
              case 0:
                _context.next = 2;
                return dispatch.agentTools.getChaosToolsList({
                  installMode: mode,
                  operateId: parsed.id
                }, function (res) {
                  res && setDataSource(res);
                });

              case 2:
              case "end":
                return _context.stop();
            }
          }
        }, _callee);
      }))();
    }, [isUpdate]);

    function handleUninstall(_x) {
      return _handleUninstall.apply(this, arguments);
    }

    function _handleUninstall() {
      _handleUninstall = (0, _asyncToGenerator2["default"])( /*#__PURE__*/regeneratorRuntime.mark(function _callee2(record) {
        return regeneratorRuntime.wrap(function _callee2$(_context2) {
          while (1) {
            switch (_context2.prev = _context2.next) {
              case 0:
                _context2.t0 = record;

                if (!_context2.t0) {
                  _context2.next = 4;
                  break;
                }

                _context2.next = 4;
                return dispatch.agentTools.uninstallChaosTools({
                  operateId: id,
                  installMode: mode,
                  name: record && record.name,
                  version: record && record.latest
                }, function (res) {
                  res && _message["default"].success(_i18n["default"].t('Successful operation'));
                  res && setIsUpdate(!isUpdate);
                });

              case 4:
              case "end":
                return _context2.stop();
            }
          }
        }, _callee2);
      }));
      return _handleUninstall.apply(this, arguments);
    }

    function handleSubmit() {
      return _handleSubmit.apply(this, arguments);
    }

    function _handleSubmit() {
      _handleSubmit = (0, _asyncToGenerator2["default"])( /*#__PURE__*/regeneratorRuntime.mark(function _callee3() {
        return regeneratorRuntime.wrap(function _callee3$(_context3) {
          while (1) {
            switch (_context3.prev = _context3.next) {
              case 0:
                setLoadingInstall(true);
                actionData && dispatch.agentTools.installChaosTools({
                  operateId: id,
                  installMode: mode,
                  toolsNamespace: 'chaosblade',
                  name: actionData && actionData.name,
                  version: actionData && actionData.latest
                }, function (res) {
                  if (res) {
                    _message["default"].success(_i18n["default"].t('Successful operation'));

                    setIsUpdate(!isUpdate);
                  }

                  setLoadingInstall(false);
                });

              case 2:
              case "end":
                return _context3.stop();
            }
          }
        }, _callee3);
      }));
      return _handleSubmit.apply(this, arguments);
    }

    var renderAction = function renderAction(value, index, record) {
      var installed = record.installed,
          unInstalled = record.unInstalled;
      record && setActionData(record);

      if (installed) {
        if (unInstalled) {
          return /*#__PURE__*/_react["default"].createElement(_button["default"], {
            text: true,
            type: "primary",
            onClick: function onClick() {
              return handleUninstall(record);
            }
          }, /*#__PURE__*/_react["default"].createElement(_Translation["default"], null, "Uninstall"));
        }

        return /*#__PURE__*/_react["default"].createElement("span", null, /*#__PURE__*/_react["default"].createElement(_Translation["default"], null, "Installed"), /*#__PURE__*/_react["default"].createElement(_icon["default"], {
          type: 'success',
          size: "small"
        }), " ");
      }

      if (!installed) return /*#__PURE__*/_react["default"].createElement(_button["default"], {
        text: true,
        type: "primary",
        loading: loadingInstall,
        onClick: function onClick() {
          return handleSubmit();
        }
      }, /*#__PURE__*/_react["default"].createElement(_Translation["default"], null, "Install"));
    };

    return /*#__PURE__*/_react["default"].createElement("div", null, /*#__PURE__*/_react["default"].createElement(_table["default"], {
      hasBorder: false,
      dataSource: dataSource,
      locale: (0, _locale["default"])().Table
    }, /*#__PURE__*/_react["default"].createElement(_table["default"].Column, {
      title: _i18n["default"].t('Tool name').toString(),
      dataIndex: "name"
    }), /*#__PURE__*/_react["default"].createElement(_table["default"].Column, {
      title: _i18n["default"].t('Latest version').toString(),
      dataIndex: "latest"
    }), /*#__PURE__*/_react["default"].createElement(_table["default"].Column, {
      title: _i18n["default"].t('Description').toString(),
      dataIndex: "description",
      width: "60%"
    }), /*#__PURE__*/_react["default"].createElement(_table["default"].Column, {
      title: _i18n["default"].t('Operation').toString(),
      cell: renderAction
    })));
  }
});

/***/ })

}]);
//# sourceMappingURL=726.bundle.js.map