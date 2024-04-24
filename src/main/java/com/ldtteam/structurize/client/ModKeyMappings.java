package com.ldtteam.structurize.client;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.api.AmecsKeyBinding;
import de.siphalor.amecs.api.KeyModifiers;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import io.github.fabricators_of_create.porting_lib.common.util.Lazy;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ModKeyMappings
{
    private static final String CATEGORY = "key.structurize.categories.general";

    /*public static final IKeyConflictContext BLUEPRINT_WINDOW = new IKeyConflictContext()
    {
        @Override
        public boolean isActive()
        {
            if (Minecraft.getInstance().screen instanceof BOScreen screen)
            {
                return screen.getWindow() instanceof AbstractBlueprintManipulationWindow;
            }
            return false;
        }

        @Override
        public boolean conflicts(IKeyConflictContext other)
        {
            return this == other;
        }
    };*/

    /**
     * Teleport using active Scan Tool
     */
    public static final Lazy<KeyMapping> TELEPORT = Lazy.of(() -> new KeyMapping("key.structurize.teleport", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), CATEGORY));

    /**
     * Move build previews
     */
    public static final Lazy<KeyMapping> MOVE_FORWARD = Lazy.of(() -> new KeyMapping("key.structurize.move_forward",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_BACK = Lazy.of(() -> new KeyMapping("key.structurize.move_back",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_LEFT = Lazy.of(() -> new KeyMapping("key.structurize.move_left",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_RIGHT = Lazy.of(() -> new KeyMapping("key.structurize.move_right",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_UP = Lazy.of(() -> new KeyMapping("key.structurize.move_up",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_ADD, CATEGORY));
    public static final Lazy<KeyMapping> MOVE_DOWN = Lazy.of(() -> new KeyMapping("key.structurize.move_down",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_KP_SUBTRACT, CATEGORY));
    public static final Lazy<KeyMapping> ROTATE_CW = Lazy.of(() -> new AmecsKeyBinding("key.structurize.rotate_cw",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT, CATEGORY, new KeyModifiers().setShift(true)));
    public static final Lazy<KeyMapping> ROTATE_CCW = Lazy.of(() -> new AmecsKeyBinding("key.structurize.rotate_ccw",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT, CATEGORY, new KeyModifiers().setShift(true)));
    public static final Lazy<KeyMapping> MIRROR = Lazy.of(() -> new KeyMapping("key.structurize.mirror",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, CATEGORY));
    public static final Lazy<KeyMapping> PLACE = Lazy.of(() -> new KeyMapping("key.structurize.place",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_ENTER, CATEGORY));

    /**
     * Register key mappings
     */
    public static void register()
    {
        KeyMappingRegistry.register(TELEPORT.get());
        KeyMappingRegistry.register(MOVE_FORWARD.get());
        KeyMappingRegistry.register(MOVE_BACK.get());
        KeyMappingRegistry.register(MOVE_LEFT.get());
        KeyMappingRegistry.register(MOVE_RIGHT.get());
        KeyMappingRegistry.register(MOVE_UP.get());
        KeyMappingRegistry.register(MOVE_DOWN.get());
        KeyMappingRegistry.register(ROTATE_CW.get());
        KeyMappingRegistry.register(ROTATE_CCW.get());
        KeyMappingRegistry.register(MIRROR.get());
        KeyMappingRegistry.register(PLACE.get());
    }

    /**
     * Private constructor to hide the implicit one.
     */
    private ModKeyMappings()
    {
        /*
         * Intentionally left empty.
         */
    }
}
