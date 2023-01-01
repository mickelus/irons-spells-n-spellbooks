package com.example.testmod.spells.evocation;

import com.example.testmod.entity.MagicMissileProjectile;
import com.example.testmod.registries.EntityRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MagicMissileSpell extends AbstractSpell {
    public MagicMissileSpell() {
        this(1);
    }

    public MagicMissileSpell(int level) {
        super(SpellType.MAGIC_MISSILE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 2;
        this.cooldown = 100;
    }

    @Override
    public void onCast(Level world, Player player) {
        MagicMissileProjectile magicMissileProjectile = new MagicMissileProjectile(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), world, player);
        magicMissileProjectile.setPos(player.position().add(0, player.getEyeHeight(), 0));
        magicMissileProjectile.shoot(player.getLookAngle());
        world.addFreshEntity(magicMissileProjectile);
    }
}
