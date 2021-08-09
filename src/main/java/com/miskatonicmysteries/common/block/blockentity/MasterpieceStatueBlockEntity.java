package com.miskatonicmysteries.common.block.blockentity;

import com.miskatonicmysteries.api.block.StatueBlock;
import com.miskatonicmysteries.api.interfaces.Affiliated;
import com.miskatonicmysteries.api.registry.Affiliation;
import com.miskatonicmysteries.common.registry.MMAffiliations;
import com.miskatonicmysteries.common.registry.MMObjects;
import com.miskatonicmysteries.common.util.Constants;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MasterpieceStatueBlockEntity extends BaseBlockEntity implements Affiliated {
    public UUID creator = null;
    public String creatorName;

    @Nullable
    private GameProfile statueOwner;

    public int pose = 0;

    public MasterpieceStatueBlockEntity(BlockPos pos, BlockState state) {
        super(MMObjects.MASTERPIECE_STATUE_BLOCK_ENTITY_TYPE, pos, state);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        if (creator != null) {
            tag.putUuid(Constants.NBT.PLAYER_UUID, creator);
        }
        if (statueOwner != null) {
            NbtCompound nbtCompound = new NbtCompound();
            NbtHelper.writeGameProfile(nbtCompound, statueOwner);
            tag.put(Constants.NBT.STATUE_OWNER, nbtCompound);
        }

        if (creatorName != null) {
            tag.putString(Constants.NBT.PLAYER_NAME, creatorName);
        }
        tag.putInt(Constants.NBT.POSE, pose);

        return super.writeNbt(tag);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        if (tag.contains(Constants.NBT.PLAYER_UUID)) {
            creator = tag.getUuid(Constants.NBT.PLAYER_UUID);
        }
        if (tag.contains(Constants.NBT.STATUE_OWNER, 10)) {
            this.setStatueProfile(NbtHelper.toGameProfile(tag.getCompound(Constants.NBT.STATUE_OWNER)));
        } else if (tag.contains("ExtraType", 8)) {
            String string = tag.getString("ExtraType");
            if (!ChatUtil.isEmpty(string)) {
                this.setStatueProfile(new GameProfile(null, string));
            }
        }
        if (tag.contains(Constants.NBT.PLAYER_NAME)) {
            creatorName = tag.getString(Constants.NBT.PLAYER_NAME);
        }
        pose = tag.getInt(Constants.NBT.POSE);
    }

    @Override
    public Affiliation getAffiliation(boolean apparent) {
        return getCachedState().getBlock() instanceof StatueBlock statue ? statue.getAffiliation(apparent) : MMAffiliations.NONE;
    }

    @Override
    public boolean isSupernatural() {
        return getCachedState().getBlock() instanceof StatueBlock statue && statue.isSupernatural();
    }

    public void setStatueProfile(@Nullable GameProfile profile) {
        synchronized(this) {
            this.statueOwner = profile;
        }

        this.loadOwnerProperties();

        if (world instanceof ServerWorld){
            sync();
        }
    }

    private void loadOwnerProperties() {
        SkullBlockEntity.loadProperties(this.statueOwner, (owner) -> {
            this.statueOwner = owner;
            this.markDirty();
        });
    }

    public GameProfile getStatueProfile() {
        return statueOwner;
    }
}
