
    xinha_editors = null;
    xinha_init    = null;
    xinha_config  = null;
    xinha_plugins = null;

    // This contains the names of textareas we will make into Xinha editors
    xinha_init = xinha_init ? xinha_init : function()
    {
      /** STEP 1 ***************************************************************
       * First, what are the plugins you will be using in the editors on this
       * page.  List all the plugins you will need, even if not all the editors
       * will use all the plugins.
       ************************************************************************/

      xinha_plugins = xinha_plugins ? xinha_plugins :
      [
       'CharacterMap',
       'CharCounter',
       'ContextMenu',
       'FullScreen',
       'ListType',
       'Stylist',
       'TableOperations',
       'DoubleClick',
       'FindReplace',
       'LuteceService',
       'QuickTag',
       'GetHtml',
       'EnterParagraphs',
       'InsertAnchor',
       'InsertSnippet',
        ];

      // THIS BIT OF JAVASCRIPT LOADS THE PLUGINS, NO TOUCHING  :)
      if(!HTMLArea.loadPlugins(xinha_plugins, xinha_init)) return;

      /** STEP 2 ***************************************************************
       * Now, what are the names of the textareas you will be turning into
       * editors?
       ************************************************************************/

      xinha_editors = xinha_editors ? xinha_editors :
      [
        'html_content'
      ];

      /** STEP 3 ***************************************************************
       * We create a default configuration to be used by all the editors.
       * If you wish to configure some of the editors differently this will be
       * done in step 4.
       *
       * If you want to modify the default config you might do something like this.
       *
       *   xinha_config = new HTMLArea.Config();
       *   xinha_config.width  = 640;
       *   xinha_config.height = 420;
       *
       *   Default configuration
       *   xinha_config = xinha_config ? xinha_config : new HTMLArea.Config();
       *
       *************************************************************************/

		    xinha_config = new HTMLArea.Config();
		    xinha_config.width  = "800px";
		    xinha_config.height = "auto";

		    xinha_config.baseHref = _site;
		    xinha_config.stripBaseHref = false; 
		    
		    xinha_config.toolbar =
                    [
                    ["popupeditor"],
                    ["separator","formatblock","bold","italic","underline","strikethrough"],
                    ["separator","textindicator"],
                    ["separator","subscript","superscript"],
                    ["linebreak","separator","justifyleft","justifycenter","justifyright","justifyfull"],
                    ["separator","insertorderedlist","insertunorderedlist","outdent","indent"],
                    ["separator","inserthorizontalrule","createlink","inserttable"],
                    ["separator","undo","redo","selectall","print"], (HTMLArea.is_gecko ? [] : ["cut","copy","paste","overwrite","saveas"]),
                    ["separator","killword","clearfonts","toggleborders","splitblock"],
                    ["separator","htmlmode","showhelp","about"]
                    ];


		    xinha_config.anchorPrefix = "jsp/site/Portal.jsp?page_id=";

		    xinha_config.pageStyle = "@import url('css/plugins/newsletter.css');";
		    xinha_config.stylistLoadStylesheet(_site + 'css/customize.css');


      /** STEP 3 ***************************************************************
       * We first create editors for the textareas.
       *
       * You can do this in two ways, either
       *
       *   xinha_editors   = HTMLArea.makeEditors(xinha_editors, xinha_config, xinha_plugins);
       *
       * if you want all the editor objects to use the same set of plugins, OR;
       *
       *   xinha_editors = HTMLArea.makeEditors(xinha_editors, xinha_config);
       *   xinha_editors['myTextArea'].registerPlugins(['Stylist','FullScreen']);
       *   xinha_editors['anotherOne'].registerPlugins(['CSS','SuperClean']);
       *
       * if you want to use a different set of plugins for one or more of the
       * editors.
       ************************************************************************/

      xinha_editors = HTMLArea.makeEditors(xinha_editors, xinha_config, xinha_plugins);

      /** STEP 4 ***************************************************************
       * If you want to change the configuration variables of any of the
       * editors,  this is the place to do that, for example you might want to
       * change the width and height of one of the editors, like this...
       *
       *   xinha_editors.myTextArea.config.width  = 640;
       *   xinha_editors.myTextArea.config.height = 480;
       *
       ************************************************************************/


      /** STEP 5 ***************************************************************
       * Finally we "start" the editors, this turns the textareas into
       * Xinha editors.
       ************************************************************************/

      HTMLArea.startEditors(xinha_editors);
    }
