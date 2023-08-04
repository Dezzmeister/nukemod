package com.dezzmeister.nukemod.explosion;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.ProtectionEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DefaultExplosionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.IExplosionContext;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

/**
 * Custom explosion implementation; mostly the same as the default explosion implementation.
 * A few changes have been made:
 * <ul>
 * <li>A variable box size is used for ray tracing instead of a fixed 16x16x16 box. The box size depends on the explosion size
 * <li>A 'blastPower' parameter determines how much the explosion damages entities
 * <li>A different formula is used to calculate entity damage due to the explosion
 * </ul>
 * 
 * @author Joe Desmond
 */
public class BigExplosion extends Explosion {
	private final boolean causesFire;
	private final boolean isGNuke;
	private final Explosion.Mode mode;
	private final Random random = new Random();
	private final World world;
	private final double x;
	private final double y;
	private final double z;
	private final float maxResistance = 10;
	private final float blastPower;
	@Nullable
	private final Entity exploder;
	private final float size;
	private final DamageSource damageSource;
	private final IExplosionContext field_234893_k_;
	private final List<BlockPos> affectedBlockPositions = Lists.newArrayList();
	private final Map<PlayerEntity, Vector3d> playerKnockbackMap = Maps.newHashMap();
	private final Vector3d position;

	public BigExplosion(World _world, @Nullable Entity _exploder, @Nullable DamageSource _damageSource,
			@Nullable IExplosionContext _context, double _x, double _y, double _z, float _size, float _blastPower, boolean _causesFire, boolean _isGNuke,
			Explosion.Mode _mode) {
		super(_world, _exploder, _damageSource, _context, _x, _y, _z, _size, _causesFire, _mode);
		this.world = _world;
		this.exploder = _exploder;
		this.size = _size;
		this.x = _x;
		this.y = _y;
		this.z = _z;
		this.blastPower = _blastPower;
		this.causesFire = _causesFire;
		this.isGNuke = _isGNuke;
		this.mode = _mode;
		this.damageSource = _damageSource == null ? DamageSource.causeExplosionDamage(this) : _damageSource;
		this.field_234893_k_ = _context == null ? this.func_234894_a_(_exploder) : _context;
		this.position = new Vector3d(this.x, this.y, this.z);
	}

	private IExplosionContext func_234894_a_(@Nullable Entity p_234894_1_) {
		return DefaultExplosionContext.INSTANCE;
	}

	public static float getBlockDensity(Vector3d p_222259_0_, Entity p_222259_1_) {
		AxisAlignedBB axisalignedbb = p_222259_1_.getBoundingBox();
		double d0 = 1.0D / ((axisalignedbb.maxX - axisalignedbb.minX) * 2.0D + 1.0D);
		double d1 = 1.0D / ((axisalignedbb.maxY - axisalignedbb.minY) * 2.0D + 1.0D);
		double d2 = 1.0D / ((axisalignedbb.maxZ - axisalignedbb.minZ) * 2.0D + 1.0D);
		double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
		double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
		if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
			int i = 0;
			int j = 0;

			for (float f = 0.0F; f <= 1.0F; f = (float) ((double) f + d0)) {
				for (float f1 = 0.0F; f1 <= 1.0F; f1 = (float) ((double) f1 + d1)) {
					for (float f2 = 0.0F; f2 <= 1.0F; f2 = (float) ((double) f2 + d2)) {
						double d5 = MathHelper.lerp((double) f, axisalignedbb.minX, axisalignedbb.maxX);
						double d6 = MathHelper.lerp((double) f1, axisalignedbb.minY, axisalignedbb.maxY);
						double d7 = MathHelper.lerp((double) f2, axisalignedbb.minZ, axisalignedbb.maxZ);
						Vector3d vector3d = new Vector3d(d5 + d3, d6, d7 + d4);
						if (p_222259_1_.world
								.rayTraceBlocks(
										new RayTraceContext(vector3d, p_222259_0_, RayTraceContext.BlockMode.COLLIDER,
												RayTraceContext.FluidMode.NONE, p_222259_1_))
								.getType() == RayTraceResult.Type.MISS) {
							++i;
						}

						++j;
					}
				}
			}

			return (float) i / (float) j;
		} else {
			return 0.0F;
		}
	}
	
	private float getBoxSize(final float size) {
		if (size <= 50) {
			return size * 2.0f;
		} else if (size < 100) {
			return size * 1.4f;
		} else {
			return size + 0.8f;
		}
	}
	
	private float cutBlastResistance(final float resistance) {
		if (resistance > maxResistance && this.isGNuke) {
			return maxResistance;
		}
		
		return resistance;
	}

	@Override
	public void doExplosionA() {
		// Blocks
		
		final int boxSize = (int) getBoxSize(size);
		final float magicNum = (float)(boxSize - 1);
		final float magicNum2 = 0.3f;
		
		Set<BlockPos> set = Sets.newHashSet();

		for (int j = 0; j < boxSize; ++j) {
			for (int k = 0; k < boxSize; ++k) {
				for (int l = 0; l < boxSize; ++l) {
					if (j == 0 || j == (boxSize - 1) || k == 0 || k == (boxSize - 1) || l == 0 || l == (boxSize - 1)) {
						// Scaled coordinates from -1 to 1 within the box
						double d0 = (double) ((float) j / magicNum * 2.0F - 1.0F);
						double d1 = (double) ((float) k / magicNum * 2.0F - 1.0F);
						double d2 = (double) ((float) l / magicNum * 2.0F - 1.0F);
						
						// Length of vector (d0, d1, d2) obtained from block position (j, k, l)
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
						
						// Normalized and scaled coordinates
						d0 = d0 / d3;
						d1 = d1 / d3;
						d2 = d2 / d3;
						
						// Random float ranging from (size + 0.7) to (size + 0.13)
						float f = this.size * (0.7F + this.world.rand.nextFloat() * 0.6F);
						double d4 = this.x;
						double d6 = this.y;
						double d8 = this.z;

						for (float f1 = 0.3F; f > 0.0F; f -= 0.22500001F) {
							BlockPos blockpos = new BlockPos(d4, d6, d8);
							BlockState blockstate = this.world.getBlockState(blockpos);
							FluidState fluidstate = this.world.getFluidState(blockpos);
							
							// Get the blast resistance
							Optional<Float> optional = this.field_234893_k_.func_230312_a_(this, this.world, blockpos,
									blockstate, fluidstate);
							
							// Subtract a function of the blast resistance of the current block
							if (optional.isPresent()) {
								f -= (cutBlastResistance(optional.get()) + 0.3F) * 0.3F;
							}
							
							// f is greater than 0 if this block did not provide enough resistance
							if (f > 0.0F
									&& this.field_234893_k_.func_230311_a_(this, this.world, blockpos, blockstate, f)) {
								set.add(blockpos);
							}
							
							
							// Ray step
							d4 += d0 * (double) magicNum2;
							d6 += d1 * (double) magicNum2;
							d8 += d2 * (double) magicNum2;
						}
					}
				}
			}
		}
		
		// Entities

		this.affectedBlockPositions.addAll(set);
		float f2 = this.size * 2.0F;
		int k1 = MathHelper.floor(this.x - (double) f2 - 1.0D);
		int l1 = MathHelper.floor(this.x + (double) f2 + 1.0D);
		int i2 = MathHelper.floor(this.y - (double) f2 - 1.0D);
		int i1 = MathHelper.floor(this.y + (double) f2 + 1.0D);
		int j2 = MathHelper.floor(this.z - (double) f2 - 1.0D);
		int j1 = MathHelper.floor(this.z + (double) f2 + 1.0D);
		List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this.exploder,
				new AxisAlignedBB((double) k1, (double) i2, (double) j2, (double) l1, (double) i1, (double) j1));
		net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.world, this, list, f2);
		Vector3d vector3d = new Vector3d(this.x, this.y, this.z);
		for (int k2 = 0; k2 < list.size(); ++k2) {
			Entity entity = list.get(k2);
			if (!entity.isImmuneToExplosions()) {
				double d12 = (double) Math.sqrt(((entity.getDistanceSq(vector3d)) / f2));
				if (d12 <= (this.size / 2)) {
					double d5 = entity.getPosX() - this.x;
					double d7 = (entity instanceof TNTEntity ? entity.getPosY() : entity.getPosYEye()) - this.y;
					double d9 = entity.getPosZ() - this.z;
					double d13 = (double) MathHelper.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
					if (d13 != 0.0D) {
						d5 = d5 / d13;
						d7 = d7 / d13;
						d9 = d9 / d13;
						double d14 = (double) getBlockDensity(vector3d, entity);
						double d10 = ((this.size / 2) - d12) * d14;
						double d15 = ((this.size / 2) - (d12 * d12)) * d14;
						final float damage = (float) (d15 * this.blastPower);
						if (damage >= 0) {
							entity.attackEntityFrom(this.getDamageSource(), damage);
						}
						double d11 = d10;
						if (entity instanceof LivingEntity) {
							d11 = ProtectionEnchantment.getBlastDamageReduction((LivingEntity) entity, d10);
						}

						entity.setMotion(entity.getMotion().add(d5 * d11, d7 * d11, d9 * d11));
						if (entity instanceof PlayerEntity) {
							PlayerEntity playerentity = (PlayerEntity) entity;
							if (!playerentity.isSpectator()
									&& (!playerentity.isCreative() || !playerentity.abilities.isFlying)) {
								this.playerKnockbackMap.put(playerentity, new Vector3d(d5 * d10, d7 * d10, d9 * d10));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Does the second part of the explosion (sound, particles, drop spawn)
	 */
	@Override
	public void doExplosionB(boolean spawnParticles) {
		if (this.world.isRemote) {
			this.world.playSound(this.x, this.y, this.z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F,
					(1.0F + (this.world.rand.nextFloat() - this.world.rand.nextFloat()) * 0.2F) * 0.7F, false);
		}

		boolean flag = this.mode != Explosion.Mode.NONE;
		if (spawnParticles) {
			if (!(this.size < 2.0F) && flag) {
				this.world.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
			} else {
				this.world.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
			}
		}

		if (flag) {
			ObjectArrayList<Pair<ItemStack, BlockPos>> objectarraylist = new ObjectArrayList<>();
			Collections.shuffle(this.affectedBlockPositions, this.world.rand);

			for (BlockPos blockpos : this.affectedBlockPositions) {
				BlockState blockstate = this.world.getBlockState(blockpos);
				if (!blockstate.isAir(this.world, blockpos)) {
					BlockPos blockpos1 = blockpos.toImmutable();
					this.world.getProfiler().startSection("explosion_blocks");
					if (blockstate.canDropFromExplosion(this.world, blockpos, this)
							&& this.world instanceof ServerWorld) {
						TileEntity tileentity = blockstate.hasTileEntity() ? this.world.getTileEntity(blockpos) : null;
						LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld) this.world))
								.withRandom(this.world.rand).withParameter(LootParameters.POSITION, blockpos)
								.withParameter(LootParameters.TOOL, ItemStack.EMPTY)
								.withNullableParameter(LootParameters.BLOCK_ENTITY, tileentity)
								.withNullableParameter(LootParameters.THIS_ENTITY, this.exploder);
						if (this.mode == Explosion.Mode.DESTROY) {
							lootcontext$builder.withParameter(LootParameters.EXPLOSION_RADIUS, this.size);
						}

						blockstate.getDrops(lootcontext$builder).forEach((p_229977_2_) -> {
							func_229976_a_(objectarraylist, p_229977_2_, blockpos1);
						});
					}

					blockstate.onBlockExploded(this.world, blockpos, this);
					this.world.getProfiler().endSection();
				}
			}

			for (Pair<ItemStack, BlockPos> pair : objectarraylist) {
				Block.spawnAsEntity(this.world, pair.getSecond(), pair.getFirst());
			}
		}

		if (this.causesFire) {
			for (BlockPos blockpos2 : this.affectedBlockPositions) {
				if (this.random.nextInt(3) == 0 && this.world.getBlockState(blockpos2).isAir()
						&& this.world.getBlockState(blockpos2.down()).isOpaqueCube(this.world, blockpos2.down())) {
					this.world.setBlockState(blockpos2, AbstractFireBlock.func_235326_a_(this.world, blockpos2));
				}
			}
		}

	}

	private static void func_229976_a_(ObjectArrayList<Pair<ItemStack, BlockPos>> p_229976_0_, ItemStack p_229976_1_,
			BlockPos p_229976_2_) {
		int i = p_229976_0_.size();

		for (int j = 0; j < i; ++j) {
			Pair<ItemStack, BlockPos> pair = p_229976_0_.get(j);
			ItemStack itemstack = pair.getFirst();
			if (ItemEntity.func_226532_a_(itemstack, p_229976_1_)) {
				ItemStack itemstack1 = ItemEntity.func_226533_a_(itemstack, p_229976_1_, 16);
				p_229976_0_.set(j, Pair.of(itemstack1, pair.getSecond()));
				if (p_229976_1_.isEmpty()) {
					return;
				}
			}
		}

		p_229976_0_.add(Pair.of(p_229976_1_, p_229976_2_));
	}

	@Override
	public DamageSource getDamageSource() {
		return this.damageSource;
	}

	@Override
	public Map<PlayerEntity, Vector3d> getPlayerKnockbackMap() {
		return this.playerKnockbackMap;
	}

	/**
	 * Returns either the entity that placed the explosive block, the entity that
	 * caused the explosion or null.
	 */
	@Nullable
	@Override
	public LivingEntity getExplosivePlacedBy() {
		if (this.exploder == null) {
			return null;
		} else if (this.exploder instanceof TNTEntity) {
			return ((TNTEntity) this.exploder).getTntPlacedBy();
		} else if (this.exploder instanceof LivingEntity) {
			return (LivingEntity) this.exploder;
		} else {
			if (this.exploder instanceof ProjectileEntity) {
				Entity entity = ((ProjectileEntity) this.exploder).func_234616_v_();
				if (entity instanceof LivingEntity) {
					return (LivingEntity) entity;
				}
			}

			return null;
		}
	}

	@Override
	public void clearAffectedBlockPositions() {
		this.affectedBlockPositions.clear();
	}

	@Override
	public List<BlockPos> getAffectedBlockPositions() {
		return this.affectedBlockPositions;
	}

	@Override
	public Vector3d getPosition() {
		return this.position;
	}

	@Nullable
	@Override
	public Entity getExploder() {
		return this.exploder;
	}
}
