package fr.flowarg.recafrpcplugin;

import javafx.scene.control.Tab;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.ui.controls.ViewportTabs;
import me.coley.recaf.ui.controls.view.ClassViewport;
import me.coley.recaf.ui.controls.view.EditorViewport;
import me.coley.recaf.ui.controls.view.FileViewport;
import me.coley.recaf.util.AccessFlag;
import org.objectweb.asm.ClassReader;

public enum FileType
{
    TEXT("text", "Editing a text file"),
    TEXT_CODE("text-code", "Editing a code file"),
    IMAGE("image", "Viewing an image file"),
    BINARY("binary", "Viewing a binary file"),
    CLASS("class", "Editing a Java class"),
    ANNOTATION("annotation", "Editing a Java annotation"),
    ENUM("enum", "Editing a Java enumeration"),
    INTERFACE("interface", "Editing a Java interface"),
    NOTHING("recaflogo", "In menus");

    private final String largeIconKey;
    private final String largeText;

    FileType(String largeIconKey, String largeText)
    {
        this.largeIconKey = largeIconKey;
        this.largeText = largeText;
    }

    public String getLargeIconKey()
    {
        return this.largeIconKey;
    }

    public String getLargeText()
    {
        return this.largeText;
    }

    public static FileType getCurrentFileType(GuiController controller)
    {
        final ViewportTabs tabs = controller.windows().getMainWindow().getTabs();
        if(tabs != null)
        {
            final Tab tab = tabs.getSelectionModel().selectedItemProperty().get();
            if(tab != null)
            {
                final EditorViewport editor = (EditorViewport)tab.getContent();
                if(editor instanceof FileViewport)
                {
                    final String path = editor.getPath();
                    switch (path.substring(path.lastIndexOf('.') + 1))
                    {
                        case "class":
                        case "css":
                        case "java":
                        case "json":
                        case "xml":
                        case "js":
                            return TEXT_CODE;
                        case "png":
                        case "gif":
                        case "jpg":
                        case "jpeg":
                        case "bmp":
                            return IMAGE;
                        case "txt":
                        case "MF":
                        case "properties":
                            return TEXT;
                        case "":
                            return NOTHING;
                        default:
                            return BINARY;
                    }
                }
                else if(editor instanceof ClassViewport)
                {
                    final ClassReader cr = controller.getWorkspace().getClassReader(editor.getPath());
                    final int flags = cr.getAccess();
                    if(AccessFlag.isAnnotation(flags))
                        return ANNOTATION;
                    else if (AccessFlag.isEnum(flags))
                        return ENUM;
                    else if(AccessFlag.isInterface(flags))
                        return INTERFACE;
                    else return CLASS;
                }
            }
        }
        return NOTHING;
    }

    public static String getCurrentAction(FileType type, String path)
    {
        if(path != null)
        {
            final String slimPath = path.substring(path.lastIndexOf('/') + 1);
            final String base = "Editing " + slimPath;

            switch (type)
            {
                case INTERFACE:
                case ANNOTATION:
                case CLASS:
                case ENUM:
                    return base + ".class";
                case TEXT:
                case TEXT_CODE:
                    return base;
                case IMAGE:
                case BINARY:
                    return "Viewing " + slimPath;
                case NOTHING:
                    return "Doing nothing";
            }
        }
        return "Doing nothing";
    }
}
