(ns ballpark-demo.core
  (:require-macros [ballpark-demo.macros :refer [timed]])
  (:require [reagent.core :as r]
            [clojure.string :as str]
            ["quick-score" :as quick-score]
            [ballpark.core :as ballpark]
            [ballpark.hiccup :as bh]
            [re-frame.core :as rf]
            [reagent.dom])
  (:import [goog.async Debouncer]))

(defn debounce [f interval]
  (let [dbnc (Debouncer. f interval)]
    (fn [& args] (.apply (.-fire dbnc) dbnc (to-array args)))))

(def raw-candidates
  [{:title "24 ways Flickr Photos On Demand with getFlickr",
    :url "http://24ways.org/2006/flickr-photos-on-demand"}
   {:title "Amberjack", :url "http://amberjack.org/"}
   {:title "DD_belatedPNG better PNG background-image support in IE6",
    :url "http://www.dillerdesign.com/experiment/DD_belatedPNG/"}
   {:title
    "DD_roundies Another (better) round-corner HTML box concept",
    :url "http://dillerdesign.com/experiment/DD_roundies/"}
   {:title
    "DEfusion.org.uk » JavaScript Image Cropper UI, using Prototype & script.aculo.us",
    :url
    "http://www.defusion.org.uk/code/javascript-image-cropper-ui-using-prototype-scriptaculous/"}
   {:title "Date Parsing [Universal Feed Parser]",
    :url "http://feedparser.org/docs/date-parsing.html"}
   {:title "Dav Glass YUI",
    :url "http://blog.davglass.com/files/yui/docs/"}
   {:title
    "Django The Web framework for perfectionists with deadlines",
    :url "http://www.djangoproject.com/"}
   {:title "Enumerating JavaScript Objects",
    :url "http://dean.edwards.name/weblog/2006/07/enum/"}
   {:title "Fleegix.js JavaScript Toolkit",
    :url "http://js.fleegix.org/?ref"}
   {:title "Google Map Parameters - Google Mapki",
    :url "http://mapki.com/wiki/Google_Map_Parameters"}
   {:title "IE PNG Fix - TwinHelix",
    :url "http://www.twinhelix.com/css/iepngfix/"}
   {:title "JSL JavaScript Standard Library",
    :url "http://www.devpro.it/JSL/"}
   {:title "JSONRequest", :url "http://json.org/JSONRequest.html"}
   {:title
    "Jack Slocum's Blog » Adding built-in editing support to the Yahoo! UI Extensions Grid",
    :url
    "http://www.jackslocum.com/yui/2006/09/10/adding-built-in-editing-support-to-the-yahoo-ui-extensions-grid/"}
   {:title
    "Javascript Toolbox Reusable Libraries And Scripts Plus Information",
    :url "http://www.javascripttoolbox.com/"}
   {:title "Javascript date parsing and formatting, Part 2 - Xaprb",
    :url
    "http://www.xaprb.com/blog/2005/12/20/javascript-date-parsing/"}
   {:title "JsonT - Transforming Json",
    :url "http://goessner.net/articles/jsont/"}
   {:title "Merlyn - Time Miscellany - J R Stockton",
    :url "http://www.merlyn.demon.co.uk/misctime.htm"}
   {:title "Minikit", :url "http://candyscript.com/projects/minikit/"}
   {:title "NextApp . Welcome", :url "http://www.nextapp.com/"}
   {:title "PhoneGap Bridging the iPhone GAP",
    :url
    "http://phonegap.com/?__vid__=Y29sbGVjdGlvblR5cGU9YWxpYXMBY29sbGVjdGlvbklEPXNjaHZlbmsBc291cmNlPXkuZGVsaWNpb3VzAWNsYXNzPWJvb2ttYXJrAXR5cGU9Ym9va21hcmsBc3VpZD1kNzE2ZGNmNWNmMTBlY2VjNjc5OGQxN2IxOTIzYTU4MA--"}
   {:title "Protoscript - Home", :url "http://protoscript.com/"}
   {:title "SIMILE Exhibit 2.0", :url "http://simile.mit.edu/exhibit/"}
   {:title "SIMILE Timeline", :url "http://simile.mit.edu/timeline/"}
   {:title "SoundManager 2 Javascript Sound for the Web",
    :url "http://www.schillmania.com/projects/soundmanager2/"}
   {:title "Sun Labs Lively Kernel",
    :url "http://research.sun.com/projects/lively/"}
   {:title "The Decorator Pattern for JavaScript",
    :url
    "http://beppu.lbox.org/articles/2006/08/22/the-decorator-pattern-for-javascript"}
   {:title "TrimJunctionCommunity - trimpath - Google Code",
    :url
    "http://code.google.com/p/trimpath/wiki/TrimJunctionCommunity"}
   {:title "Truncate A jQuery Plugin",
    :url "http://www.reindel.com/truncate/"}
   {:title "Windows Media Object Model Reference for Scripting",
    :url "http://msdn2.microsoft.com/en-us/library/aa392281.aspx"}
   {:title "YUI Loader Script",
    :url "http://blog.davglass.com/files/yui/loader/"}
   {:title
    "YouTube Chromeless Player Reference - YouTube APIs and Tools - Google Code",
    :url
    "http://code.google.com/apis/youtube/chromeless_player_reference.html"}
   {:title "[haXe.org]", :url "http://haxe.org/intro"}
   {:title "actsAsAspect()",
    :url "http://beppu.lbox.org/articles/2006/09/06/actsasaspect"}
   {:title
    "http--www.odessa.net-docs-javascr-DOSFILES-COLORS-CSET.HTM",
    :url "http://www.odessa.net/docs/javascr/DOSFILES/COLORS/CSET.HTM"}
   {:title "jQuery Sparklines",
    :url
    "http://www.omnipotent.net/jquery.sparkline/?__vid__=Y29sbGVjdGlvblR5cGU9YWxpYXMBY29sbGVjdGlvbklEPWNhbnRvbmliAXNvdXJjZT15LmRlbGljaW91cwFjbGFzcz1ib29rbWFyawF0eXBlPWJvb2ttYXJrAXN1aWQ9ZTZkODhhYzc3NDg5ZDA3YTY5Y2QxOWFkMmZiZmNhMWY-"}
   {:title "jsonxml", :url "http://goessner.net/download/prj/jsonxml/"}
   {:title "jspax documentations", :url "http://www.jspax.org/"}
   {:title "moo.fx - size does matter",
    :url "http://moofx.mad4milk.net/"}
   {:title "oEmbed", :url "http://www.oembed.com/"}
   {:title "qooxdoo » Home", :url "http://qooxdoo.org/"}
   {:title "strftime strftime for Javascript",
    :url "http://hacks.bluesmoon.info/strftime/"}
   {:title "Online Documentation",
    :url "http://www.jackslocum.com/docs/"}
   {:title "bartaz/impress.js - GitHub",
    :url "https://github.com/bartaz/impress.js"}
   {:title "Xmarks Features: Overview",
    :url "http://www.xmarks.com/about/features/overview"}
   {:title "Bootstrap, from Twitter",
    :url "http://twitter.github.com/bootstrap/"}
   {:title
    "slimbox - The ultimate lightweight Lightbox clone - Google Project Hosting",
    :url "http://code.google.com/p/slimbox/"}
   {:title "blur.js", :url "http://www.blurjs.com/"}
   {:title
    "Foundation: Rapid Prototyping and Building Framework from ZURB",
    :url "http://foundation.zurb.com/docs/index.php"}
   {:title
    "canvg - Javascript SVG parser and renderer on Canvas - Google Project Hosting",
    :url "http://code.google.com/p/canvg/"}
   {:title "kangax/fabric.js",
    :url "https://github.com/kangax/fabric.js"}
   {:title "Moment.js - A lightweight javascript date library",
    :url "http://momentjs.com/"}
   {:title "Esprima", :url "http://esprima.org/"}
   {:title "Constellation/escodegen · GitHub",
    :url "https://github.com/Constellation/escodegen"}
   {:title "log4javascript - a JavaScript logging framework",
    :url "http://log4javascript.org/"}
   {:title "dgrid - A New Dojo grid created by SitePen",
    :url "http://dojofoundation.org/packages/dgrid/"}
   {:title "FuseJS JavaScript Framework", :url "http://fusejs.com/"}
   {:title "bestiejs/maddy", :url "https://github.com/bestiejs/maddy"}
   {:title "GLUEscript | Glueing Libraries Using EcmaScript",
    :url "http://gluescript.sourceforge.net/"}
   {:title "object.watch polyfill — Gist",
    :url "https://gist.github.com/384583"}
   {:title "eriwen/javascript-stacktrace",
    :url "https://github.com/eriwen/javascript-stacktrace"}
   {:title "Vows « Asynchronous BDD for Node",
    :url "http://vowsjs.org/"}
   {:title "Jasmine: BDD for your JavaScript",
    :url "http://pivotal.github.com/jasmine/"}
   {:title
    "JSON-js/json2.js at master · douglascrockford/JSON-js · GitHub",
    :url
    "https://github.com/douglascrockford/JSON-js/blob/master/json2.js"}
   {:title "Underscore.string",
    :url "http://epeli.github.com/underscore.string/"}
   {:title "PEG.js – Parser Generator for JavaScript",
    :url "http://pegjs.majda.cz/"}
   {:title "Brunch", :url "http://brunch.io/"}
   {:title "chaplinjs/chaplin",
    :url "https://github.com/chaplinjs/chaplin"}
   {:title "joshaven/string_score",
    :url "https://github.com/joshaven/string_score"}
   {:title "requestAnimationFrame for smart animating « Paul Irish",
    :url
    "http://paulirish.com/2011/requestanimationframe-for-smart-animating/"}
   {:title "Backbone UI", :url "http://perka.github.com/backbone-ui/"}
   {:title "REST API - Evri",
    :url "http://www.evri.com/developer/rest"}
   {:title "Redactor WYSIWYG editor on jQuery",
    :url "http://redactorjs.com/"}
   {:title "Wirefy | The Responsive Wireframe Boilerplate",
    :url "http://cjdsie.github.com/wirefy/"}
   {:title "TaffyDB - The JavaScript Database",
    :url "http://www.taffydb.com/"}
   {:title "slightlyoff/cassowary-js-refactor",
    :url "https://github.com/slightlyoff/cassowary-js-refactor"}
   {:title "AppJS", :url "http://appjs.org/"}
   {:title "IndexedDB Polyfill",
    :url "http://nparashuram.com/IndexedDBShim/"}
   {:title
    "Rich JavaScript Applications – the Seven Frameworks (Throne of JS, 2012) - Steve Sanderson’s blog - As seen on YouTube™",
    :url
    "http://blog.stevensanderson.com/2012/08/01/rich-javascript-applications-the-seven-frameworks-throne-of-js-2012/"}
   {:title "AngularJS — Superheroic JavaScript MVW Framework",
    :url "http://angularjs.org/"}
   {:title "Meteor", :url "http://www.meteor.com/main"}
   {:title "JSCSSP, a CSS parser in JavaScript",
    :url "http://www.glazman.org/JSCSSP/"}
   {:title "NV/CSSOM", :url "https://github.com/NV/CSSOM"}
   {:title "Jison / Demos",
    :url "http://zaach.github.com/jison/demos/"}
   {:title "jQuery Zoom", :url "http://www.jacklmoore.com/zoom"}
   {:title "lukaszkorecki/RequestPack",
    :url "https://github.com/lukaszkorecki/RequestPack"}
   {:title "Jade - Template Engine", :url "http://jade-lang.com/"}
   {:title "evilstreak/markdown-js · GitHub",
    :url "https://github.com/evilstreak/markdown-js"}
   {:title "Magnific Popup: Responsive jQuery Lightbox Plugin",
    :url
    "http://dimsemenov.com/plugins/magnific-popup/?utm_source=twitter&utm_medium=twitter"}
   {:title "Home · kripken/emscripten Wiki · GitHub",
    :url "https://github.com/kripken/emscripten/wiki"}
   {:title "Adobe® Edge Inspect Getting Started Guide",
    :url
    "chrome-extension://ijoeapleklopieoejahbpdnhkjjgddem/firstrun.html"}
   {:title "Two.js", :url "http://jonobr1.github.io/two.js/"}
   {:title "Ratchet", :url "http://maker.github.io/ratchet/"}
   {:title "D3.js - Data-Driven Documents", :url "http://d3js.org/"}
   {:title "ashima/webgl-noise",
    :url "https://github.com/ashima/webgl-noise"}
   {:title "ashima/webgl-noise",
    :url "https://github.com/ashima/webgl-noise/"}
   {:title "Experiments with Perlin noise - Blog - Clicktorelease",
    :url
    "http://www.clicktorelease.com/blog/experiments-with-perlin-noise"}
   {:title
    "lunr.js - A bit like Solr, but much smaller and not as bright",
    :url "http://lunrjs.com/"}
   {:title "Faster UI Animations With Velocity.js | Smashing Magazine",
    :url
    "http://www.smashingmagazine.com/2014/06/18/faster-ui-animations-with-velocity-js/?utm_source=html5weekly&utm_medium=email"}
   {:title
    "PhysicsJS - A modular, extendable, and easy-to-use physics engine for javascript",
    :url "http://wellcaffeinated.net/PhysicsJS/"}
   {:title "Papa Parse - Powerful CSV parser for Javascript",
    :url
    "http://papaparse.com/?utm_source=javascriptweekly&utm_medium=email"}
   {:title "Machina.js - Finite State Machines in JavaScript",
    :url "http://machina-js.org/"}
   {:title
    "hello.js - Javascript API for OAuth2 authentication and REST services",
    :url
    "http://adodson.com/hello.js/?utm_source=javascriptweekly&utm_medium=email#core-methods"}
   {:title "trueinteractions/tint2 · GitHub",
    :url
    "https://github.com/trueinteractions/tint2?utm_source=html5weekly&utm_medium=email"}
   {:title "☆ sweep.js ☆",
    :url
    "http://rileyjshaw.com/sweep/?utm_source=javascriptweekly&utm_medium=email"}
   {:title "mortzdk/localStorage · GitHub",
    :url
    "https://github.com/mortzdk/localStorage/?utm_source=javascriptweekly&utm_medium=email"}
   {:title "aehlke/tag-it · GitHub",
    :url "https://github.com/aehlke/tag-it"}
   {:title "jxnblk/fitter-happier-text",
    :url "https://github.com/jxnblk/fitter-happier-text"}
   {:title "andreaferretti/paths-js",
    :url "https://github.com/andreaferretti/paths-js"}
   {:title
    "gifshot - JavaScript library that can create animated GIFs from media streams, videos, or images",
    :url "http://yahoo.github.io/gifshot/"}
   {:title "Nunjucks", :url "http://mozilla.github.io/nunjucks/"}
   {:title "cthackers/adm-zip",
    :url "https://github.com/cthackers/adm-zip"}
   {:title "FormatJS", :url "http://formatjs.io/"}
   {:title "nodejitsu/forever",
    :url "https://github.com/nodejitsu/forever"}
   {:title "JMPerez/spotify-web-api-js",
    :url "https://github.com/jmperez/spotify-web-api-js"}
   {:title "Firebase - Build Realtime Apps",
    :url "https://www.firebase.com/"}
   {:title "adobe-webplatform/dropcap.js",
    :url
    "https://github.com/adobe-webplatform/dropcap.js?utm_source=html5weekly&utm_medium=email"}
   {:title "rackt/react-router",
    :url "https://github.com/rackt/react-router"}
   {:title "Page.js by visionmedia",
    :url "http://visionmedia.github.io/page.js/"}
   {:title "flatiron/cradle",
    :url "https://github.com/flatiron/cradle"}
   {:title "Fuse.js | K. Risk - JavaScript Refined",
    :url "http://kiro.me/projects/fuse.html"}
   {:title "rmm5t/liquidmetal",
    :url "https://github.com/rmm5t/liquidmetal"}
   {:title "willlma/fuzzyset.js",
    :url "https://github.com/willlma/fuzzyset.js"}
   {:title "zdyn/jaro-winkler-js",
    :url "https://github.com/zdyn/jaro-winkler-js"}
   {:title
    "Web Services v2 Documentation | Climate Data Online (CDO) | National Climatic Data Center (NCDC)",
    :url
    "http://www.ncdc.noaa.gov/cdo-web/webservices/v2#gettingStarted"}
   {:title "API | Weather Underground",
    :url "http://www.wunderground.com/weather/api/d/pricing.html"}
   {:title "API Guide | restify",
    :url "http://mcavage.me/node-restify/"}
   {:title "seatgeek/react-infinite",
    :url "https://github.com/seatgeek/react-infinite"}
   {:title "debug", :url "https://www.npmjs.org/package/debug"}
   {:title "notatestuser/gift · GitHub",
    :url "https://github.com/notatestuser/gift"}
   {:title "kofrasa/mingo · GitHub",
    :url
    "https://github.com/kofrasa/mingo?utm_source=javascriptweekly&utm_medium=email"}
   {:title "Tuxx",
    :url
    "http://www.tuxedojs.org/?utm_source=javascriptweekly&utm_medium=email"}
   {:title "gabelerner/canvg · GitHub",
    :url
    "https://github.com/gabelerner/canvg?utm_source=javascriptweekly&utm_medium=email"}
   {:title "Apache Thrift - Home", :url "https://thrift.apache.org/"}
   {:title "facebook/css-layout",
    :url "https://github.com/facebook/css-layout"}
   {:title "Hjson, the Human JSON",
    :url
    "http://hjson.org/?utm_source=javascriptweekly&utm_medium=email"}
   {:title "mpowaga/react-slider",
    :url "https://github.com/mpowaga/react-slider"}
   {:title "TheaterJS, a typing effect mimicking human behavior.",
    :url "http://gabinaureche.com/TheaterJS/#"}
   {:title "mrdoob/texgen.js",
    :url
    "https://github.com/mrdoob/texgen.js?utm_source=javascriptweekly&utm_medium=email"}
   {:title "Defiant.js", :url "http://defiantjs.com/"}
   {:title "henrikingo/xml2json",
    :url "https://github.com/henrikingo/xml2json"}
   {:title
    "Rickshaw: A JavaScript toolkit for creating interactive time-series graphs",
    :url "http://code.shutterstock.com/rickshaw/"}
   {:title "Immutable.js",
    :url "http://facebook.github.io/immutable-js/"}
   {:title "Flipboard/react-canvas",
    :url "https://github.com/flipboard/react-canvas"}
   {:title "PDFKit", :url "http://pdfkit.org/"}
   {:title "yahoo/fluxible", :url "https://github.com/yahoo/fluxible"}
   {:title "vivus.js - svg animation",
    :url
    "http://maxwellito.github.io/vivus/?utm_source=html5weekly&utm_medium=email"}
   {:title "parse5 by inikulin",
    :url
    "http://inikulin.github.io/parse5/?utm_source=html5weekly&utm_medium=email"}
   {:title "Adding Audio to Web Apps -Telerik Developer Network",
    :url
    "http://developer.telerik.com/featured/adding-audio-to-web-apps/?utm_source=html5weekly&utm_medium=email"}
   {:title "Cross-Platform Native Development with Javascript",
    :url "https://www.nativescript.org/"}
   {:title "Strand Web Components",
    :url
    "http://mediamath.github.io/strand/?utm_source=html5weekly&utm_medium=email"}
   {:title "ramjet",
    :url
    "http://www.rich-harris.co.uk/ramjet/?utm_source=html5weekly&utm_medium=email"}
   {:title "Manipulating Images on Web Pages with CamanJS",
    :url
    "http://www.sitepoint.com/manipulating-images-web-pages-camanjs/?utm_source=html5weekly&utm_medium=email"}
   {:title "ExpandJS",
    :url
    "http://www.expandjs.com/?utm_source=html5weekly&utm_medium=email"}
   {:title "spoike/refluxjs",
    :url "https://github.com/spoike/refluxjs"}
   {:title "peachananr/onepage-scroll",
    :url "https://github.com/peachananr/onepage-scroll"}
   {:title "FixedDataTable",
    :url "https://facebook.github.io/fixed-data-table/"}
   {:title "bling dot js",
    :url
    "https://gist.github.com/paulirish/12fb951a8b893a454b32?utm_source=javascriptweekly&utm_medium=email"}
   {:title
    "Relay | A JavaScript framework for building data-driven React applications",
    :url
    "https://facebook.github.io/relay/?utm_source=javascriptweekly&utm_medium=email"}
   {:title "cmpolis/smart-table-scroll · GitHub",
    :url
    "https://github.com/cmpolis/smart-table-scroll?utm_source=javascriptweekly&utm_medium=email"}
   {:title "rackt/redux · GitHub",
    :url "https://github.com/rackt/redux"}
   {:title "Substance",
    :url
    "http://substance.io/?utm_source=javascriptweekly&utm_medium=email"}
   {:title "json.human.js - Json Formatting for Human Beings",
    :url "http://marianoguerra.github.io/json.human.js/"}
   {:title "beautify-web/js-beautify · GitHub",
    :url "https://github.com/beautify-web/js-beautify"}
   {:title "yesmeck/jquery-jsonview · GitHub",
    :url "https://github.com/yesmeck/jquery-jsonview"}
   {:title "GriddleGriddle/Griddle · GitHub",
    :url "https://github.com/GriddleGriddle/Griddle"}
   {:title "reactabular - Spectacular tables for React.js",
    :url "http://bebraw.github.io/reactabular/"}
   {:title "nmn/react-infinity · GitHub",
    :url "https://github.com/nmn/react-infinity"}
   {:title "pedronauck/react-simpletabs · GitHub",
    :url "https://github.com/pedronauck/react-simpletabs"}
   {:title "vis.js - A dynamic, browser based visualization library.",
    :url "http://visjs.org/"}
   {:title "Datavisualization.ch Selected Tools",
    :url "http://selection.datavisualization.ch/"}
   {:title "flitbit/diff · GitHub",
    :url "https://github.com/flitbit/diff"}
   {:title "Tixit/odiff", :url "https://github.com/Tixit/odiff"}
   {:title "bripkens/fuzzy.js",
    :url "https://github.com/bripkens/fuzzy.js"}
   {:title "krisk/Fuse", :url "https://github.com/krisk/fuse"}
   {:title "atom/fuzzaldrin",
    :url "https://github.com/atom/fuzzaldrin/"}
   {:title "jwagner/smartcrop.js",
    :url
    "https://github.com/jwagner/smartcrop.js?utm_source=javascriptweekly&utm_medium=email"}
   {:title "mikach/requirejs-babel",
    :url "https://github.com/mikach/requirejs-babel"}
   {:title "Top Open-Source Static Site Generators - StaticGen",
    :url "https://www.staticgen.com/"}
   {:title "HTML Cleaner - Word To HTML Converter",
    :url "http://www.html-cleaner.com/"}
   {:title "marsdb", :url "https://www.npmjs.com/package/marsdb"}
   {:title "STRML/react-grid-layout",
    :url "https://github.com/STRML/react-grid-layout"}
   {:title "NW.js", :url "http://nwjs.io/"}
   {:title
    "laktek.github.io/jQuery-Smart-Auto-Complete/demo/qs_score.js",
    :url
    "http://laktek.github.io/jQuery-Smart-Auto-Complete/demo/qs_score.js"}
   {:title "Master Complex Redux Workflows with Sagas",
    :url
    "http://konkle.us/master-complex-redux-workflows-with-sagas/?utm_source=javascriptweekly&utm_medium=email"}
   {:title
    "GitHub - BlueOakJS/blueoak-server: express.js-based, swagger-matic, server runtime",
    :url
    "https://github.com/BlueOakJS/blueoak-server?utm_source=javascriptweekly&utm_medium=email"}
   {:title "Our Justified Layout Goes Open Source | code.flickr.com",
    :url
    "http://code.flickr.net/2016/04/05/our-justified-layout-goes-open-source/"}
   {:title "RethinkDB: the open-source database for the realtime web",
    :url "https://www.rethinkdb.com/"}
   {:title
    "GitHub - facundoolano/promise-log: shortcut for console.loggin' your promises",
    :url
    "https://github.com/facundoolano/promise-log?utm_source=javascriptweekly&utm_medium=email"}
   {:title "Pusher | Leader In Realtime Technologies",
    :url "https://pusher.com/"}
   {:title "Cleave.js - Format input text content when you are typing",
    :url "http://nosir.github.io/cleave.js/"}
   {:title "lodash documentation", :url "https://lodash.com/docs"}
   {:title
    "felixrieseberg/electron-uwp-background: Sample explaining how to use UWP background tasks with Electron apps",
    :url "https://github.com/felixrieseberg/electron-uwp-background"}
   {:title
    "GitHub - ebidel/appmetrics.js: A small (1.1kb) library for measuring things in your web app and reporting the results to Google Analytics.",
    :url
    "https://github.com/ebidel/appmetrics.js?utm_source=html5weekly&utm_medium=email"}
   {:title
    "GitHub - fulcrumapp/react-virtual-grid: Virtual Grid for React",
    :url "https://github.com/fulcrumapp/react-virtual-grid"}
   {:title "Sinon.JS - Documentation", :url "http://sinonjs.org/docs/"}
   {:title
    "GitHub - nolanlawson/optimize-js: Optimize a JavaScript file for faster initial load by wrapping eagerly-invoked functions",
    :url "https://github.com/nolanlawson/optimize-js"}
   {:title
    "GitHub - nolanlawson/marky: High-resolution JavaScript timer based on performance.mark() and measure()",
    :url "https://github.com/nolanlawson/marky"}
   {:title "Front-end Tools: My Favorite Finds of 2016",
    :url
    "https://www.sitepoint.com/front-end-tools-my-favorite-finds-of-2016/?utm_source=frontendfocus&utm_medium=email"}
   {:title
    "GitHub - styled-components/styled-components: Visual primitives for the component age 💅",
    :url "https://github.com/styled-components/styled-components"}
   {:title "Rebass", :url "http://jxnblk.com/rebass/"}
   {:title
    "GitHub - sindresorhus/electron-debug: Adds useful debug features to your Electron app",
    :url "https://github.com/sindresorhus/electron-debug"}
   {:title "Elemental UI", :url "http://elemental-ui.com/"}
   {:title
    "GitHub - brillout/awesome-react-components: Catalog of React Components & Libraries",
    :url "https://github.com/brillout/awesome-react-components"}
   {:title
    "GitHub - chentsulin/electron-react-boilerplate: Live editing development on desktop app",
    :url "https://github.com/chentsulin/electron-react-boilerplate"}
   {:title
    "GitHub - gabrielbull/react-desktop: React UI Components for macOS Sierra and Windows 10",
    :url "https://github.com/gabrielbull/react-desktop"}
   {:title
    "reactjs/redux: Predictable state container for JavaScript apps",
    :url "https://github.com/reactjs/redux"}
   {:title "EmojiOne | The Open Emoji Standard",
    :url "http://emojione.com/"}
   {:title "twitter/twemoji: Twitter Emoji for Everyone",
    :url "https://github.com/twitter/twemoji"}
   {:title "kolodny/wavy: use ~ in require and import calls",
    :url "https://github.com/kolodny/wavy"}
   {:title "Photon · Components",
    :url "http://photonkit.com/components/"}
   {:title
    "electron-userland/electron-forge: A complete tool for creating, publishing, and installing modern Electron applications",
    :url "https://github.com/electron-userland/electron-forge/"}
   {:title "styled-jsx",
    :url "https://www.npmjs.com/package/styled-jsx"}
   {:title
    "FormidableLabs/radium: A toolchain for React component styling.",
    :url "https://github.com/FormidableLabs/radium"}
   {:title "Complementary Tools · facebook/react Wiki",
    :url "https://github.com/facebook/react/wiki/Complementary-Tools"}
   {:title
    "developit/preact: ⚛️ Fast 3kb React alternative with the same ES6 API. Components & Virtual DOM.",
    :url "https://github.com/developit/preact"}
   {:title "Editor · Slate", :url "https://docs.slatejs.org/#"}
   {:title
    "xyc/react-inspector: Power of Browser DevTools inspectors right inside your React app",
    :url "https://github.com/xyc/react-inspector"}
   {:title "atom/node-keytar: Native Password Node Module",
    :url "https://github.com/atom/node-keytar"}
   {:title "Draft.js | Rich Text Editor Framework for React",
    :url "https://draftjs.org/"}
   {:title "valuelink", :url "https://www.npmjs.com/package/valuelink"}
   {:title
    "epoberezkin/ajv: The fastest JSON-Schema Validator. Supports draft-04/06",
    :url "https://github.com/epoberezkin/ajv"}
   {:title "typicode/husky: Git hooks made easy",
    :url "https://github.com/typicode/husky"}
   {:title "react-tabs/style at master · reactjs/react-tabs",
    :url "https://github.com/reactjs/react-tabs/tree/master/style"}
   {:title "MUI - Material Design CSS Framework",
    :url "https://www.muicss.com/"}
   {:title
    "nakajmg/electron-search-text: electron findInPage wrapper module",
    :url "https://github.com/nakajmg/electron-search-text"}
   {:title "electron-in-page-search",
    :url "https://www.npmjs.com/package/electron-in-page-search"}
   {:title
    "jrowny/react-absolute-grid: An absolutely positioned, animated, filterable, sortable, drag and droppable, ES6 grid for React.",
    :url "https://github.com/jrowny/react-absolute-grid"}
   {:title "bevacqua/react-dragula: Drag and drop so simple it hurts",
    :url "https://github.com/bevacqua/react-dragula"}
   {:title
    "jaredreich/pell: 📝 the simplest and smallest (1kB) WYSIWYG text editor for web, with no dependencies",
    :url
    "https://github.com/jaredreich/pell?utm_source=frontendfocus&utm_medium=email"}
   {:title
    "clauderic/react-sortable-hoc: ✌️ A set of higher-order components to turn any list into an animated, touch-friendly, sortable list.",
    :url "https://github.com/clauderic/react-sortable-hoc"}
   {:title "react-dnd/react-dnd: Drag and Drop for React",
    :url "https://github.com/react-dnd/react-dnd"}
   {:title
    "ianstormtaylor/slate: A completely customizable framework for building rich text editors.",
    :url
    "https://github.com/ianstormtaylor/slate?utm_source=javascriptweekly&utm_medium=email"}
   {:title "Diet.js", :url "http://dietjs.com/"}
   {:title "zeit/next.js: Framework for server-rendered React apps",
    :url "https://github.com/zeit/next.js"}
   {:title
    "acdlite/recompose: A React utility belt for function components and higher-order components.",
    :url "https://github.com/acdlite/recompose"}
   {:title
    "mozilla/webextension-polyfill: A lightweight polyfill library for Promise-based WebExtension APIs in Chrome",
    :url "https://github.com/mozilla/webextension-polyfill"}
   {:title "bodymovin/bodymovin: after effects to html library",
    :url "https://github.com/bodymovin/bodymovin"}
   {:title
    "tfoxy/chrome-promise: Promises for chrome JavaScript APIs used in extensions and apps.",
    :url "https://github.com/tfoxy/chrome-promise"}
   {:title
    "pages-themes/minimal: Minimal is a Jekyll theme for GitHub Pages",
    :url "https://github.com/pages-themes/minimal"}
   {:title "react-chrome-extension-boilerplate",
    :url
    "https://www.npmjs.com/package/react-chrome-extension-boilerplate"}
   {:title
    "JedWatson/classnames: A simple javascript utility for conditionally joining classNames together",
    :url "https://github.com/JedWatson/classnames"}
   {:title "A library for building cross-platform apps - ReactXP",
    :url "https://microsoft.github.io/reactxp/"}
   {:title "emoji-mart",
    :url "https://www.npmjs.com/package/emoji-mart"}
   {:title
    "atlassian/react-beautiful-dnd: Beautiful, accessible drag and drop for lists with React.js",
    :url
    "https://github.com/atlassian/react-beautiful-dnd?utm_source=reactnl&utm_medium=email#readme"}
   {:title "Easy and fast SDK for beautiful apps - Flutter",
    :url "https://flutter.io/"}
   {:title "Feather – Simply beautiful open source icons",
    :url "https://feathericons.com/"}
   {:title "Welcome to Workbox", :url "https://workboxjs.org/"}
   {:title "Minio", :url "https://www.minio.io/"}
   {:title "Push v1.0 | Javascript Notification Framework",
    :url "https://pushjs.org/"}
   {:title
    "bvaughn/react-virtualized: React components for efficiently rendering large lists and tabular data",
    :url "https://github.com/bvaughn/react-virtualized"}
   {:title "reshape/reshape: transform html with javascript plugins",
    :url "https://github.com/reshape/reshape"}
   {:title "PrimeReact",
    :url "https://www.primefaces.org/primereact/#/"}
   {:title
    "ayrton/react-key-handler: React component to handle keyboard events",
    :url "https://github.com/ayrton/react-key-handler"}
   {:title
    "avocode/react-shortcuts: Manage keyboard shortcuts from one place",
    :url "https://github.com/avocode/react-shortcuts"}
   {:title "react-shortcuts",
    :url "https://www.npmjs.com/package/react-shortcuts"}
   {:title "react-keydown",
    :url "https://www.npmjs.com/package/react-keydown"}
   {:title "lodash CDN by jsDelivr - A CDN for npm and GitHub",
    :url "https://www.jsdelivr.com/package/npm/lodash"}
   {:title "react-html-document",
    :url "https://www.npmjs.com/package/react-html-document"}
   {:title "Supplying Images - Google Chrome",
    :url "https://developer.chrome.com/webstore/images"}
   {:title
    "electron-userland/electron-webpack: Scripts and configurations to compile Electron applications using webpack",
    :url "https://github.com/electron-userland/electron-webpack"}
   {:title "workco/marvin: React and Redux, Webpack 2 boilerplate",
    :url "https://github.com/workco/marvin"}
   {:title
    "babel/minify: An ES6+ aware minifier based on the Babel toolchain (beta)",
    :url "https://github.com/babel/minify"}
   {:title
    "plenluno/promise-mutex: Mutual-exclusion lock for promise chains",
    :url "https://github.com/plenluno/promise-mutex"}
   {:title "antvis/g2: G2 (The Grammar of Graphics)",
    :url "https://github.com/antvis/g2"}
   {:title "olov/stringmap: Fast and robust stringmap for JavaScript",
    :url "https://github.com/olov/stringmap"}
   {:title
    "freesoftwarefactory/parse-multipart: A javascript/nodejs multipart/form-data parser which operates on raw data.",
    :url "https://github.com/freesoftwarefactory/parse-multipart"}
   {:title
    "react-tools/react-form: React Form - Powerful and lightweight forms in React",
    :url "https://github.com/react-tools/react-form"}
   {:title "Popmotion - A functional JavaScript motion library",
    :url "https://popmotion.io/"}
   {:title "grunt-remove-logging",
    :url "https://www.npmjs.com/package/grunt-remove-logging"}
   {:title "IconBros",
    :url "https://www.iconbros.com/?ref=producthunt"}
   {:title
    "NUKnightLab/TimelineJS3: TimelineJS v3: A Storytelling Timeline built in JavaScript. http://timeline.knightlab.com",
    :url "https://github.com/NUKnightLab/TimelineJS3"}
   {:title
    "timeline - vis.js - A dynamic, browser based visualization library.",
    :url "http://visjs.org/docs/timeline/"}
   {:title
    "jamiebuilds/unstated: State so simple, it goes without saying",
    :url "https://github.com/jamiebuilds/unstated"}
   {:title
    "Home - Proton Native - React Native for the desktop, cross compatible",
    :url "https://proton-native.js.org/#/"}
   {:title "xpl/crx-hotreload: Chrome Extension Hot Reloader",
    :url "https://github.com/xpl/crx-hotreload"}
   {:title
    "larsenwork/postcss-easing-gradients: PostCSS plugin to create smooth linear-gradients that approximate easing functions.",
    :url "https://github.com/larsenwork/postcss-easing-gradients"}
   {:title "slick - the last carousel you'll ever need",
    :url "http://kenwheeler.github.io/slick/"}
   {:title
    "jakesgordon/javascript-state-machine: A javascript finite state machine library",
    :url "https://github.com/jakesgordon/javascript-state-machine"}
   {:title
    "atlassian/react-beautiful-dnd: Beautiful, accessible drag and drop for lists with React.js",
    :url "https://github.com/atlassian/react-beautiful-dnd"}
   {:title
    "Glide.js | A dependency-free JavaScript ES6 slider and carousel",
    :url "https://glidejs.com/"}
   {:title "Epic Spinners", :url "http://epic-spinners.epicmax.co/#/"}
   {:title "planttheidea/fast-copy: A blazing fast deep object copier",
    :url "https://github.com/planttheidea/fast-copy"}
   {:title "klauscfhq/signale: 👋 Hackable console logger",
    :url "https://github.com/klauscfhq/signale"}
   {:title
    "jaywcjlove/hotkeys: ➷ A robust Javascript library for capturing keyboard input. It has no dependencies.",
    :url "https://github.com/jaywcjlove/hotkeys"}
   {:title "Material-UI", :url "https://material-ui.com/"}
   {:title
    "rubenspgcavalcante/webpack-chrome-extension-reloader: 🔥 Hot reloading while developing Chrome extensions with webpack 🔥",
    :url
    "https://github.com/rubenspgcavalcante/webpack-chrome-extension-reloader"}
   {:title
    "timarney/react-app-rewired: Override create-react-app webpack configs without ejecting",
    :url "https://github.com/timarney/react-app-rewired"}
   {:title
    "gajus/write-file-webpack-plugin: Forces webpack-dev-server to write bundle files to the file system.",
    :url "https://github.com/gajus/write-file-webpack-plugin"}
   {:title
    "acvetkov/sinon-chrome: Testing chrome extensions with Node.js",
    :url "https://github.com/acvetkov/sinon-chrome"}
   {:title
    "adazzle/react-data-grid: Excel-like grid component built with React, with editors, keyboard navigation, copy & paste, and the like http://adazzle.github.io/react-data-grid/",
    :url "https://github.com/adazzle/react-data-grid"}
   {:title
    "react-data-grid/Cell.js at master · adazzle/react-data-grid",
    :url
    "https://github.com/adazzle/react-data-grid/blob/master/packages/react-data-grid/src/Cell.js"}
   {:title
    "Scrum/webpack-extension-manifest-plugin: Creates manifest json file based you config",
    :url "https://github.com/Scrum/webpack-extension-manifest-plugin"}
   {:title "fast-memoize - npm",
    :url "https://www.npmjs.com/package/fast-memoize"}
   {:title "QuicKey – The quick tab switcher - Chrome Web Store",
    :url
    "https://chrome.google.com/webstore/detail/quickey-%E2%80%93-the-quick-tab-s/ldlghkoiihaelfnggonhjnfiabmaficg"}
   {:title
    "QuicKey | Jump between recent tabs in Chrome via keyboard or menu",
    :url "https://fwextensions.github.io/QuicKey/"}])

(def candidates raw-candidates)

(def candidates-js (clj->js raw-candidates))


(def bs-config (assoc ballpark/base-config :scorer ballpark/score-standard))

(defn ballpark [query]
  (take 25
        (timed "   ballpark"
               (ballpark/quick-score-collection candidates [:title :url] query bs-config))))

(def qs-config (assoc ballpark/base-config :scorer ballpark/score-quickey))

(defn quickscore [query]
  (take 25
        (timed "quick-score"
               (ballpark/quick-score-collection candidates [:title :url] query qs-config))))

(rf/reg-event-db
 ::score-results
 (fn [db _]
   (let [query (get db ::query)]
     (-> db
         (assoc-in [::results :quick-score] (quickscore query))
         (assoc-in [::results :ballpark] (ballpark query))))))

(rf/reg-event-fx
 ::update-query
 (fn [{:keys [db]} [_ query]]
   {:db (assoc-in db [::query] query)
    :dispatch [::score-results]}))

(rf/reg-sub
 ::results
 (fn [db [_ lib]]
   (get-in db [::results lib])))

(rf/reg-sub
 ::query
 (fn [db _]
   (get db ::query)))

(defn score [field-score]
  (when-let [title-score (some-> field-score)]
    [:code.score (.toFixed title-score 5)]))

(defn list-item [{{:keys [url title]} :item matches :matches scores :scores}]
  [:div
   [:div.title
    (into [:p]
          (bh/highlight-regions title (:title matches)))
    [score (get scores :title 0)]]
   [:div.url
    (into [:p] (bh/highlight-regions url (:url matches)))
    [score (get scores :url 0)]]])

(defn app []
  (let [query @(rf/subscribe [::query])
        ballpark @(rf/subscribe [::results :ballpark])
        quick-score (vec @(rf/subscribe [::results :quick-score]))]
    [:section.main
     [:input {:type "text"
              :auto-focus true
              :value query
              :on-change (fn [e]
                           (rf/dispatch [::update-query (.. e -target -value)]))}]
     [:div.lists
      [:ul.link-list.ballpark
       (for [[i {:keys [item] :as candidate}] (map-indexed vector ballpark)]
         ^{:key (:url item)} [:li [list-item candidate]])]
      [:ul.link-list.quick-score
       (for [{:keys [item] :as candidate} quick-score]
         ^{:key (:url item)} [:li [list-item candidate]])]]]))

(defn ^:export ^:dev/after-load main []
  (.log js/console "Loading app...")
  (reagent.dom/render [app] (js/document.getElementById "app")))
