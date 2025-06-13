package fr.supermax_8.endertranslate.core;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public class WrapperPlayServerUpdateAdvancements extends PacketWrapper<WrapperPlayServerUpdateAdvancements> {
    private boolean reset;
    private Advancement[] advancements;
    private String[] removedAdvancements;
    private Map<String, List<AdvancementProgress>> progress;// key = advancement id
    private Optional<Boolean> showAdvancements = Optional.empty(); // 1.21.5+

    public WrapperPlayServerUpdateAdvancements(PacketSendEvent event) {
        super(event);
    }

    public WrapperPlayServerUpdateAdvancements(boolean reset, Advancement[] advancements, String[] removedAdvancements, Map<String, List<AdvancementProgress>> progress, Optional<Boolean> showAdvancements) {
        super(PacketType.Play.Server.UPDATE_ADVANCEMENTS);
        this.reset = reset;
        this.advancements = advancements;
        this.removedAdvancements = removedAdvancements;
        this.progress = progress;
        this.showAdvancements = showAdvancements;
    }

    @Override
    public void read() {
        reset = readBoolean();
        advancements = new Advancement[readVarInt()];
        for (int i = 0; i < advancements.length; i++) {
            String id = readString();
            String parentId = readBoolean() ? readString() : null;
            AdvancementDisplay display = null;
            if (readBoolean()) {
                Component title = readComponent();
                Component description = readComponent();
                ItemStack icon = readItemStack();
                AdvancementFrameType frameType = AdvancementFrameType.getById(readVarInt());
                int flags = readInt();

                boolean hasBackgroundTexture = (flags & AdvancementDisplay.SHOW_BACKGROUND_TEXTURE) != 0;
                boolean showToast = (flags & AdvancementDisplay.SHOW_TOAST) != 0;
                boolean hidden = (flags & AdvancementDisplay.HIDDEN) != 0;
                String backgroundTexture = hasBackgroundTexture ? readString() : null;

                float x = readFloat();
                float y = readFloat();
                display = new AdvancementDisplay(title, description, icon, frameType, showToast, hidden, backgroundTexture, x, y);
            }

            Optional<String[]> criteria = Optional.empty();
            if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_20_1)) {
                String[] criteriaArray = new String[readVarInt()];
                for (int j = 0; j < criteriaArray.length; j++) {
                    criteriaArray[j] = readString();
                }
                criteria = Optional.of(criteriaArray);
            }
            List<String[]> requirements = new ArrayList<>();
            int requirementsSize = readVarInt();
            for (int j = 0; j < requirementsSize; j++) {
                String[] requirement = new String[readVarInt()];
                for (int k = 0; k < requirement.length; k++) {
                    requirement[k] = readString();
                }
                requirements.add(requirement);
            }
            Optional<Boolean> sendsTelemetryData = Optional.empty();
            if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20)) {
                sendsTelemetryData = Optional.of(readBoolean());
            }
            advancements[i] = new Advancement(id, parentId, display, criteria, requirements, sendsTelemetryData);
        }
        removedAdvancements = new String[readVarInt()];
        for (int i = 0; i < removedAdvancements.length; i++) {
            removedAdvancements[i] = readString();
        }
        progress = readMap(PacketWrapper::readString, packetWrapper -> {
            List<AdvancementProgress> list = new ArrayList<>();
            int size = packetWrapper.readVarInt();
            for (int i = 0; i < size; i++) {
                String criteria = packetWrapper.readString();
                Optional<Long> time = Optional.empty();
                if (readBoolean()) {
                    time = Optional.of(readLong());
                }
                list.add(new AdvancementProgress(criteria, time));
            }
            return list;
        });
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
            showAdvancements = Optional.of(readBoolean());
        }
    }

    @Override
    public void write() {
        writeBoolean(reset);
        writeVarInt(advancements.length);
        for (Advancement advancement : advancements) {
            writeString(advancement.getId());
            if (advancement.getParentId() != null) {
                writeBoolean(true);
                writeString(advancement.getParentId());
            } else {
                writeBoolean(false);
            }
            AdvancementDisplay display = advancement.getDisplay();
            writeBoolean(display != null);
            if (display != null) {
                writeComponent(display.getTitle());
                writeComponent(display.getDescription());
                writeItemStack(display.getIcon());
                writeVarInt(display.getFrameType().getId());
                writeInt(display.getFlags());
                if (display.getBackgroundTexture() != null) {
                    writeString(display.getBackgroundTexture());
                }
                writeFloat(display.getX());
                writeFloat(display.getY());
            }
            if (serverVersion.isOlderThanOrEquals(ServerVersion.V_1_20_1)) {
                String[] criteriaArray = advancement.getCriteria().orElse(new String[0]);
                writeVarInt(criteriaArray.length);
                for (String criteria : criteriaArray) {
                    writeString(criteria);
                }
            }
            writeVarInt(advancement.getRequirements().size());
            for (String[] requirement : advancement.getRequirements()) {
                writeVarInt(requirement.length);
                for (String s : requirement) {
                    writeString(s);
                }
            }
            if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_20)) {
                writeBoolean(advancement.isSendsTelemetryData().orElse(false));
            }
        }
        writeVarInt(removedAdvancements.length);
        for (String removedAdvancement : removedAdvancements) {
            writeString(removedAdvancement);
        }
        writeMap(progress, PacketWrapper::writeString, (packetWrapper, progresses) -> {
            packetWrapper.writeVarInt(progresses.size());
            for (AdvancementProgress advancementProgress : progresses) {
                packetWrapper.writeString(advancementProgress.getCriteriaId());
                if (advancementProgress.getTime().isPresent()) {
                    packetWrapper.writeBoolean(true);
                    packetWrapper.writeLong(advancementProgress.getTime().get());
                } else {
                    packetWrapper.writeBoolean(false);
                }
            }
        });
        if (serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
            writeBoolean(showAdvancements.orElse(false));
        }
    }

    @Override
    public void copy(WrapperPlayServerUpdateAdvancements wrapper) {
        this.reset = wrapper.reset;
        this.advancements = wrapper.advancements;
        this.removedAdvancements = wrapper.removedAdvancements;
        this.progress = wrapper.progress;
        this.showAdvancements = wrapper.showAdvancements;
    }

    public boolean isReset() {
        return reset;
    }

    public Advancement[] getAdvancements() {
        return advancements;
    }

    public String[] getRemovedAdvancements() {
        return removedAdvancements;
    }

    public Map<String, List<AdvancementProgress>> getProgress() {
        return progress;
    }

    public Optional<Boolean> getShowAdvancements() {
        return showAdvancements;
    }

    public void setReset(boolean reset) {
        this.reset = reset;
    }

    public void setAdvancements(Advancement[] advancements) {
        this.advancements = advancements;
    }

    public void setRemovedAdvancements(String[] removedAdvancements) {
        this.removedAdvancements = removedAdvancements;
    }

    public void setProgress(Map<String, List<AdvancementProgress>> progress) {
        this.progress = progress;
    }

    public void setShowAdvancements(Optional<Boolean> showAdvancements) {
        this.showAdvancements = showAdvancements;
    }

    public static class Advancement {
        private String id;
        private String parentId;
        private AdvancementDisplay display;
        // 1.20.1-
        private Optional<String[]> criteria;
        private List<String[]> requirements;
        // 1.20+
        private Optional<Boolean> sendsTelemetryData;

        public Advancement(String id, String parentId, AdvancementDisplay display, Optional<String[]> criteria, List<String[]> requirements, Optional<Boolean> sendsTelemetryData) {
            this.id = id;
            this.parentId = parentId;
            this.display = display;
            this.criteria = criteria;
            this.requirements = requirements;
            this.sendsTelemetryData = sendsTelemetryData;
        }

        public String getId() {
            return id;
        }

        public String getParentId() {
            return parentId;
        }

        public AdvancementDisplay getDisplay() {
            return display;
        }

        public Optional<String[]> getCriteria() {
            return criteria;
        }

        public List<String[]> getRequirements() {
            return requirements;
        }

        public Optional<Boolean> isSendsTelemetryData() {
            return sendsTelemetryData;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public void setDisplay(AdvancementDisplay display) {
            this.display = display;
        }

        public void setCriteria(Optional<String[]> criteria) {
            this.criteria = criteria;
        }

        public void setRequirements(List<String[]> requirements) {
            this.requirements = requirements;
        }

        public void setSendsTelemetryData(Optional<Boolean> sendsTelemetryData) {
            this.sendsTelemetryData = sendsTelemetryData;
        }
    }

    public static class AdvancementDisplay {
        public static final int SHOW_BACKGROUND_TEXTURE = 0x01;
        public static final int SHOW_TOAST = 0x02;
        public static final int HIDDEN = 0x04;
        private Component title;
        private Component description;
        private ItemStack icon;
        private AdvancementFrameType frameType;
        private boolean showToast;
        private boolean hidden;
        @Nullable
        private String backgroundTexture;
        private float x;
        private float y;

        public AdvancementDisplay(Component title, Component description, ItemStack icon, AdvancementFrameType frameType, boolean showToast, boolean hidden, @Nullable String backgroundTexture, float x, float y) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.frameType = frameType;
            this.showToast = showToast;
            this.hidden = hidden;
            this.backgroundTexture = backgroundTexture;
            this.x = x;
            this.y = y;
        }

        public int getFlags() {
            int flags = 0;
            if (backgroundTexture != null) {
                flags |= SHOW_BACKGROUND_TEXTURE;
            }
            if (showToast) {
                flags |= SHOW_TOAST;
            }
            if (hidden) {
                flags |= HIDDEN;
            }
            return flags;
        }

        public Component getTitle() {
            return title;
        }

        public Component getDescription() {
            return description;
        }

        public ItemStack getIcon() {
            return icon;
        }

        public AdvancementFrameType getFrameType() {
            return frameType;
        }

        public boolean isShowToast() {
            return showToast;
        }

        public boolean isHidden() {
            return hidden;
        }

        @Nullable
        public String getBackgroundTexture() {
            return backgroundTexture;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public void setTitle(Component title) {
            this.title = title;
        }

        public void setDescription(Component description) {
            this.description = description;
        }

        public void setIcon(ItemStack icon) {
            this.icon = icon;
        }

        public void setFrameType(AdvancementFrameType frameType) {
            this.frameType = frameType;
        }

        public void setShowToast(boolean showToast) {
            this.showToast = showToast;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public void setBackgroundTexture(@Nullable String backgroundTexture) {
            this.backgroundTexture = backgroundTexture;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    public enum AdvancementFrameType {
        TASK, CHALLENGE, GOAL;

        public static AdvancementFrameType getById(int id) {
            return values()[id];
        }

        public int getId() {
            return ordinal();
        }
    }

    public static class AdvancementProgress {
        private String criteriaId;
        private Optional<Long> time;

        public AdvancementProgress(String criteriaId, Optional<Long> time) {
            this.criteriaId = criteriaId;
            this.time = time;
        }

        public AdvancementProgress(String criteriaId) {
            this(criteriaId, Optional.empty());
        }

        public String getCriteriaId() {
            return criteriaId;
        }

        public Optional<Long> getTime() {
            return time;
        }

        public void setCriteriaId(String criteriaId) {
            this.criteriaId = criteriaId;
        }

        public void setTime(Optional<Long> time) {
            this.time = time;
        }
    }
}