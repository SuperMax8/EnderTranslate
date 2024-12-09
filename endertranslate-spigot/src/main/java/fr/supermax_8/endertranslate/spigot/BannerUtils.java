package fr.supermax_8.endertranslate.spigot;

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
            DyeColor color;
            try {
                color = DyeColor.values()[Integer.parseInt(split[1])];
            } catch (Exception e) {
                color = DyeColor.valueOf(split[1].toUpperCase());
            }
            meta.addPattern(new Pattern(color, PatternType.getByIdentifier(split[0])));
        }
        meta.setDisplayName(title);
        meta.addItemFlags(ItemFlag.values());
        banner.setItemMeta(meta);
        return banner;
    }

}