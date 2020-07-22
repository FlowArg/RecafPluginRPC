package fr.flowarg.recafrpcplugin;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import javafx.scene.control.Tab;
import me.coley.recaf.config.Conf;
import me.coley.recaf.control.Controller;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.plugin.api.ConfigurablePlugin;
import me.coley.recaf.plugin.api.StartupPlugin;
import me.coley.recaf.ui.controls.ViewportTabs;
import me.coley.recaf.ui.controls.view.ClassViewport;
import me.coley.recaf.ui.controls.view.ClassViewport.ClassMode;
import me.coley.recaf.ui.controls.view.EditorViewport;
import org.plugface.core.annotations.Plugin;

@Plugin(name = "RPC")
public class RPCPlugin implements ConfigurablePlugin, StartupPlugin
{
    @Conf("rpc.enable")
    public boolean rpcEnabled = true;

    @Override
    public void onStart(Controller controller)
    {
        final GuiController guiController = (GuiController)controller;
        if(this.rpcEnabled)
        {
            final DiscordRPC lib = DiscordRPC.INSTANCE;
            final DiscordEventHandlers handlers = new DiscordEventHandlers();
            handlers.ready = (user) -> System.out.println(String.format("Setup discord rich presence for %s#%s (%s)", user.username, user.discriminator, user.userId));
            lib.Discord_Initialize("731147624605810698", handlers, false, "");
            final DiscordRichPresence rpc = new DiscordRichPresence();
            rpc.startTimestamp = System.currentTimeMillis() / 1000;
            rpc.smallImageKey = "recaflogo";
            rpc.smallImageText = "Recaf decompiler";
            this.updateRPC(rpc, guiController);
            lib.Discord_UpdatePresence(rpc);

            final Thread rpcThread = new Thread(() -> {
                if(this.rpcEnabled)
                {
                    while (!Thread.currentThread().isInterrupted())
                    {
                        if(this.rpcEnabled)
                        {
                            try
                            {
                                this.updateRPC(rpc, guiController);
                                lib.Discord_UpdatePresence(rpc);
                                lib.Discord_RunCallbacks();
                                Thread.sleep(200);
                            } catch (InterruptedException ignored) {}
                        }
                        else this.shutdownRPC(lib, Thread.currentThread());
                    }
                }
                else this.shutdownRPC(lib, Thread.currentThread());
            }, "RPC-Callback-Handler");
            rpcThread.start();
        }
    }

    private synchronized void shutdownRPC(DiscordRPC lib, Thread thread)
    {
        lib.Discord_Shutdown();
        thread.interrupt();
    }

    private synchronized void updateRPC(DiscordRichPresence rpc, GuiController controller)
    {
        final FileType fileType = FileType.getCurrentFileType(controller);
        rpc.largeImageText = fileType.getLargeText();
        rpc.largeImageKey = fileType.getLargeIconKey();
        final ObjectsStorage<ClassMode, EditorViewport> modeNViewport = this.getEditorType(controller);
        if(modeNViewport != null)
        {
            final ClassMode mode = modeNViewport.getFirstObject();
            if(modeNViewport.getSecondObject() != null && modeNViewport.getSecondObject().getPath() != null)
                rpc.details = FileType.getCurrentAction(fileType, modeNViewport.getSecondObject().getPath());
            else rpc.details = "";

            if(mode != null)
            {
                switch (mode)
                {
                    case DECOMPILE:
                        rpc.state = "In Decompile mode.";
                        break;
                    case TABLE:
                        rpc.state = "In Table mode.";
                        break;
                    case HEX:
                        rpc.state = "In Hexadecimal mode.";
                        break;
                    default:
                        rpc.state = "";
                }
            }
            else rpc.state = "";
        }
        else
        {
            rpc.state = "";
            rpc.details = "";
        }
    }

    private ObjectsStorage<ClassMode, EditorViewport> getEditorType(GuiController controller)
    {
        final ViewportTabs tabs = controller.windows().getMainWindow().getTabs();
        if(tabs != null)
        {
            final Tab tab = tabs.getSelectionModel().selectedItemProperty().get();
            if(tab != null)
            {
                final EditorViewport editor = (EditorViewport)tab.getContent();
                if(editor instanceof ClassViewport)
                    return new ObjectsStorage<>(((ClassViewport)editor).getClassMode(), (ClassViewport)editor);
                else return new ObjectsStorage<>(null, editor);
            }
        }
        return null;
    }

    @Override
    public String getVersion()
    {
        return "1.1.2";
    }

    @Override
    public String getDescription()
    {
        return "A Discord RichPresence plugin for Recaf";
    }

    @Override
    public String getConfigTabTitle()
    {
        return "RPC configuration";
    }
}
