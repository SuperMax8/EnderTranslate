package fr.supermax_8.endertranslate.bungee;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.adventure.AdventureSerializer;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import io.github.retrooper.packetevents.bungee.factory.BungeePacketEventsBuilder;
import net.kyori.adventure.Adventure;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EndertranslateBungee extends Plugin {

    @Override
    public void onLoad() {
        PacketEvents.setAPI(BungeePacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().reEncodeByDefault(false)
                .checkForUpdates(true)
                .bStats(true);

        //On Bukkit, calling this here is essential, hence the name "load"
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        // Plugin startup logic

        PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract() {
            public void onPacketReceive(PacketReceiveEvent event) {
            }

            public void onPacketSend(PacketSendEvent e) {
                PacketTypeCommon packetType = e.getPacketType();
                if (!(packetType instanceof PacketType.Play.Server type)) return;
                switch (type) {
                    case SYSTEM_CHAT_MESSAGE -> {
                        WrapperPlayServerSystemChatMessage packet = new WrapperPlayServerSystemChatMessage(e);
                        applyTranslateOnPacketSend(e, packet::getMessage, packet::setMessage);
                    }
                    case ACTION_BAR -> {
                        WrapperPlayServerActionBar packet = new WrapperPlayServerActionBar(e);
                        applyTranslateOnPacketSend(e, packet::getActionBarText, packet::setActionBarText);
                    }
                    case TITLE -> {
                        WrapperPlayServerTitle packet = new WrapperPlayServerTitle(e);
                        applyTranslateOnPacketSend(e, packet::getTitle, packet::setTitle);
                        applyTranslateOnPacketSend(e, packet::getSubtitle, packet::setSubtitle);
                    }
                    case SET_TITLE_TEXT -> {
                        WrapperPlayServerSetTitleText packet = new WrapperPlayServerSetTitleText(e);
                        applyTranslateOnPacketSend(e, packet::getTitle, packet::setTitle);
                    }
                    case SET_TITLE_SUBTITLE -> {
                        WrapperPlayServerSetTitleSubtitle packet = new WrapperPlayServerSetTitleSubtitle(e);
                        applyTranslateOnPacketSend(e, packet::getSubtitle, packet::setSubtitle);
                    }
                    case OPEN_WINDOW -> {
                        WrapperPlayServerOpenWindow packet = new WrapperPlayServerOpenWindow(e);
                        applyTranslateOnPacketSend(e, packet::getTitle, packet::setTitle);
                    }
                    case ENTITY_METADATA -> {
                        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(e);
                        for (EntityData data : packet.getEntityMetadata()) {
                            if (data.getType() != EntityDataTypes.OPTIONAL_ADV_COMPONENT) continue;
                            Optional<Component> name = (Optional<Component>) data.getValue();
                            if (name.isPresent())
                                applyTranslateOnPacketSend(e, name::get, comp -> data.setValue(Optional.of(comp)));
                            return;
                        }
                    }
                    case WINDOW_ITEMS -> {
                        WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(e);
                        applyTranslateOnItemStacks(e, packet.getItems());
                    }
                    case SET_SLOT -> {
                        WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(e);
                        applyTranslateOnItemStack(e, packet.getItem());
                    }
                    case DISCONNECT -> {
                        WrapperPlayServerDisconnect packet = new WrapperPlayServerDisconnect(e);
                        applyTranslateOnPacketSend(e, packet::getReason, packet::setReason);
                    }
                    case BOSS_BAR -> {
                        WrapperPlayServerBossBar packet = new WrapperPlayServerBossBar(e);
                        if (packet.getAction() instanceof WrapperPlayServerBossBar.UpdateTitleAction titleAction) {
                            applyTranslateOnPacketSend(e, () -> titleAction.title, comp -> titleAction.title = comp);
                        }
                    }
                }
            }
        });

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public void applyTranslateOnItemStacks(PacketSendEvent e, Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks)
            applyTranslateOnItemStack(e, stack);
    }

    public void applyTranslateOnItemStack(PacketSendEvent e, ItemStack stack) {
        boolean modified = false;
        NBTCompound nbt = stack.getNBT();
        if (nbt == null) return;
        Map<String, NBT> tags = nbt.getTags();

        // Translate book pages for signed books
        if (tags.containsKey("pages")) {
            NBTList<NBTString> newPages = null;
            NBTList<NBTString> pages = (NBTList<NBTString>) tags.get("pages");
            for (NBTString page : pages.getTags()) {
                String translated = applyTranslate(page.getValue());
                if (translated == null) continue;
                if (newPages == null) newPages = new NBTList<>(NBTType.STRING);
                newPages.addTag(new NBTString(translated));
            }
            if (newPages != null) nbt.setTag("pages", newPages);
            modified = true;
        }


        NBTCompound display = (NBTCompound) tags.get("display");
        if (display != null) {
            // Translate name
            Map<String, NBT> displayTags = display.getTags();
            if (displayTags.containsKey("Name")) {
                String name = ((NBTString) displayTags.get("Name")).getValue();
                String translated = applyTranslate(name);
                if (translated != null) {
                    modified = true;
                    display.setTag("Name", new NBTString(translated));
                }
            }

            // Translate lore
            if (displayTags.containsKey("Lore")) {
                NBTList<NBTString> lore = (NBTList<NBTString>) displayTags.get("Lore");
                NBTList<NBTString> newLore = null;
                for (NBTString line : lore.getTags()) {
                    String translated = applyTranslate(line.getValue());
                    if (translated == null) continue;
                    if (newLore == null) newLore = new NBTList<>(NBTType.STRING);
                    newLore.addTag(new NBTString(translated));
                }
                if (newLore != null) {
                    display.setTag("Lore", newLore);
                    modified = true;
                }
            }
        }

        if (modified) e.markForReEncode(true);
    }

    public void applyTranslateOnPacketSend(PacketSendEvent e, Supplier<Component> getMessage, Consumer<Component> setMessage) {
        Component toTranslate = getMessage.get();
        if (toTranslate == null) return;
        String message = AdventureSerializer.toLegacyFormat(toTranslate);
        String translated = applyTranslate(message);
        if (translated != null) {
            setMessage.accept(AdventureSerializer.fromLegacyFormat(translated));
            e.markForReEncode(true);
        }
    }

    public String applyTranslate(String toTranslate) {
        System.out.println(toTranslate);
        String translated = toTranslate.replace("test", "ZOUGZOUG");
        return translated.equals(toTranslate) ? null : translated;
    }


}