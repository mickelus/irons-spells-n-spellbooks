package com.example.testmod.capabilities.magic;

import com.example.testmod.entity.AbstractConeProjectile;
import com.example.testmod.network.ClientBoundSyncPlayerData;
import com.example.testmod.network.ServerboundCancelCast;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class PlayerMagicData {

    public PlayerMagicData() {
    }

    public PlayerMagicData(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    public void setServerPlayer(ServerPlayer serverPlayer) {
        if (this.serverPlayer == null) {
            this.serverPlayer = serverPlayer;
        }
    }

    private ServerPlayer serverPlayer = null;
    public static final String MANA = "mana";
    public static final String COOLDOWNS = "cooldowns";

    /********* MANA *******************************************************/

    private int mana;

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = mana;
    }

    public void addMana(int mana) {
        this.mana += mana;
    }

    /********* SYNC DATA *******************************************************/

    private PlayerSyncedData playerSyncedData;

    public PlayerSyncedData getSyncedData() {
        if (playerSyncedData == null) {
            playerSyncedData = new PlayerSyncedData(serverPlayer);
        }

        return playerSyncedData;
    }

    public void syncToPlayer(ServerPlayer serverPlayer) {
        Messages.sendToPlayer(new ClientBoundSyncPlayerData(getSyncedData()), serverPlayer);
    }

    /********* CASTING *******************************************************/

    private boolean isCasting = false;
    private int castingSpellId = 0;
    private int castingSpellLevel = 0;
    private int castDurationRemaining = 0;
    private CastSource castSource;

    public AbstractConeProjectile cone;
    private int castDuration = 0;
    private ItemStack castingItemStack = ItemStack.EMPTY;
    private Vec3 teleportTargetPosition;

    public void resetCastingState() {
        this.isCasting = false;
        this.castingSpellId = 0;
        this.castingSpellLevel = 0;
        this.castDurationRemaining = 0;
        this.teleportTargetPosition = null;
        this.discardCone();
    }

    public static void serverSideCancelCast(ServerPlayer serverPlayer, PlayerMagicData playerMagicData) {
        ServerboundCancelCast.cancelCast(serverPlayer, SpellType.values()[playerMagicData.getCastingSpellId()].getCastType() == CastType.CONTINUOUS);
    }

    public void initiateCast(int castingSpellId, int castingSpellLevel, int castDuration, CastSource castSource) {
        this.castSource = castSource;
        this.castingSpellId = castingSpellId;
        this.castingSpellLevel = castingSpellLevel;
        this.castDuration = castDuration;
        this.castDurationRemaining = castDuration;
        this.isCasting = true;
    }

    public boolean discardCone() {
        if (this.cone != null) {
            this.cone.discard();
            this.cone = null;
            //TestMod.LOGGER.debug("PlayerMagicData: discarding cone");
            return true;
        }
        return false;
    }

    public void setTeleportTargetPosition(Vec3 targetPosition) {
        this.teleportTargetPosition = targetPosition;
    }

    public Vec3 getTeleportTargetPosition() {
        return this.teleportTargetPosition;
    }

    public boolean isCasting() {
        return isCasting;
    }

    public int getCastingSpellId() {
        return castingSpellId;
    }

    public int getCastingSpellLevel() {
        return castingSpellLevel;
    }

    public CastSource getCastSource() {
        return castSource;
    }

    public int getCastDurationRemaining() {
        return castDurationRemaining;
    }

    public void handleCastDuration() {
        castDurationRemaining--;

        if (castDurationRemaining <= 0) {
            isCasting = false;
            castDurationRemaining = 0;
        }
    }

    public void setPlayerCastingItem(ItemStack itemStack) {
        this.castingItemStack = itemStack;
    }

    public ItemStack getPlayerCastingItem() {
        return this.castingItemStack;
    }

    /********* COOLDOWNS *******************************************************/

    private final PlayerCooldowns playerCooldowns = new PlayerCooldowns();

    public PlayerCooldowns getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    /********* SYSTEM *******************************************************/

    public static PlayerMagicData getPlayerMagicData(ServerPlayer serverPlayer) {
        var capContainer = serverPlayer.getCapability(PlayerMagicProvider.PLAYER_MAGIC);
        if (capContainer.isPresent()) {
            var opt = capContainer.resolve();
            if (opt.isEmpty()) {
                return new PlayerMagicData(serverPlayer);
            }

            var pmd = opt.get();
            pmd.setServerPlayer(serverPlayer);
            return pmd;
        }
        return new PlayerMagicData(serverPlayer);
    }

    public void saveNBTData(CompoundTag compound) {
        //TestMod.LOGGER.debug("PlayerMagicData: saving nbt");
        compound.putInt(MANA, mana);

        if (playerCooldowns.hasCooldownsActive()) {
            ListTag listTag = new ListTag();
            playerCooldowns.saveNBTData(listTag);
            if (!listTag.isEmpty()) {
                compound.put(COOLDOWNS, listTag);
            }
        }
    }

    public void loadNBTData(CompoundTag compound) {
        //TestMod.LOGGER.debug("PlayerMagicData: loading nbt");
        mana = compound.getInt(MANA);
        ListTag listTag = (ListTag) compound.get(COOLDOWNS);

        if (listTag != null && !listTag.isEmpty()) {
            playerCooldowns.loadNBTData(listTag);
        }

    }
}
