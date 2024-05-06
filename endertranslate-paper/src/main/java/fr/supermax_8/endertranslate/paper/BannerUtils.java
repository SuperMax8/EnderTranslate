package fr.supermax_8.endertranslate.paper;


import net.kyori.adventure.text.Component;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;

import java.util.List;

public class BannerUtils {



    public static ItemStack getBanner(String title, String material, List<String> patterns) {
        ItemStack banner = new ItemStack(Material.getMaterial(material));
        BannerMeta meta = (BannerMeta) banner.getItemMeta();
        for (String pattern : patterns) {
            String[] split = pattern.split(";");
            meta.addPattern(new Pattern(DyeColor.values()[Integer.parseInt(split[1])], PatternType.getByIdentifier(split[0])));
        }
        meta.displayName(Component.text(title));
        meta.addItemFlags(ItemFlag.values());
        banner.setItemMeta(meta);
        return banner;
    }






}