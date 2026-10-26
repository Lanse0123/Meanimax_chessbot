package lanse.worldwarchess.shared.core;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.*;

/** Written by Lanse, taken from Bonk Universe
 * <br><br>
 * This converts a class into bytecode. Uncomment it in the Main class to use it. **/

//To use this, uncomment the imports in build.gradle.kts, and then uncomment this code.
public class BytecodeExplorer {

    public static void dumpClass(Class<?> clazz) {
        try {
            File output = new File("src/main/resources/" + clazz.getSimpleName() + ".txt");

            output.getParentFile().mkdirs();

            try (InputStream input = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
                 PrintWriter writer = new PrintWriter(new FileWriter(output))) {

                if (input == null) {
                    throw new RuntimeException("Failed to find bytecode or something because input is null");
                }

                ClassReader reader = new ClassReader(input);

                TraceClassVisitor visitor = new TraceClassVisitor(writer);

                reader.accept(visitor, ClassReader.SKIP_DEBUG);

                System.out.println("Bytecode written to: " + output.getAbsolutePath());
            }
        } catch (Exception ignored){}
    }
}

//DO NOT UNCOMMENT BELOW
///////////////////////////////////////////////////////////////////////////////////////////////////////


/** Minecraft Mixin that I made for Enchanted Golden Apple override: **/
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.ModifyArg;
//
//import java.util.List;
//
//@Mixin(targets = "net.minecraft.world.item.Items")
//public class EnchantedGoldenAppleMixin {
//
//    @ModifyArg(
//            method = "<clinit>", //java bytecode name for Class Initialization Method, for things like static{}
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/world/item/Item$Properties;food(Lnet/minecraft/world/food/FoodProperties;Lnet/minecraft/world/item/component/Consumable;)Lnet/minecraft/world/item/Item$Properties;"),
//            index = 1
//    )
//    private static Consumable modifyGoldenAppleConsumable(Consumable original) {
//        if (original == Consumables.ENCHANTED_GOLDEN_APPLE) {
//            return Consumable.builder()
//                    .onConsume(new ApplyStatusEffectsConsumeEffect(List.of(
//                            new MobEffectInstance(MobEffects.REGENERATION, 400, 1),
//                            new MobEffectInstance(MobEffects.RESISTANCE, 2400, 0),
//                            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0),
//                            new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0)
//                    )))
//                    .build();
//        }
//
//        return original;
//    }
//}
//mixins are so painful yet so powerful