package cz.nejakejtomas.bluemapbanners.mixin;

import cz.nejakejtomas.bluemapbanners.MarkerAPI;
import cz.nejakejtomas.bluemapbanners.markers.maps.Map;
import cz.nejakejtomas.bluemapbanners.markers.maps.MapName;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.OptionalInt;

@Mixin(ItemFrameEntity.class)
public abstract class AbstractItemFrameEntityMixin extends AbstractDecorationEntity
{
	private final Logger LOGGER = LoggerFactory.getLogger(AbstractItemFrameEntityMixin.class);
	@Shadow
	private boolean fixed;
	
	protected AbstractItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> entityType, World world) {
		super(entityType, world);
	}
	
	protected AbstractItemFrameEntityMixin(EntityType<? extends AbstractDecorationEntity> type, World world, BlockPos pos) {
		super(type, world, pos);
	}
	
	@Shadow
	public abstract OptionalInt getMapId();
	
	@Shadow
	public abstract ItemStack getHeldItemStack();
	
	@Inject(at = @At("HEAD"), method = "dropHeldStack(Lnet/minecraft/entity/Entity;Z)V", require = 1)
	private void removeFromFrame(@Nullable Entity entity, boolean alwaysDrop, CallbackInfo ci) {
		if (fixed)
			return;
		String holderName = entity == null ? "null" : entity.getName().getString();
		String holderUuid = entity == null ? "null" : entity.getUuidAsString();
		
		Map map = getMap();
		if (map == null)
			return;
		
		if (MarkerAPI.INSTANCE.removeMarker(map)) {
			LOGGER.info("Map marker removed " + map.getPositionX() + " " + map.getPositionY() + " " + map.getPositionZ() + " by " + holderName + " " + holderUuid + " in " + map.getDimension());
		}
	}
	
	@Inject(method = "Lnet/minecraft/entity/decoration/ItemFrameEntity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ItemFrameEntity;setHeldItemStack(Lnet/minecraft/item/ItemStack;)V", shift = At.Shift.AFTER))
	void interact(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		Map map = getMap();
		if (map == null)
			return;
		
		if (MarkerAPI.INSTANCE.addMarker(map)) {
			LOGGER.info("Map marker created " + map.getPositionX() + " " + map.getPositionY() + " " + map.getPositionZ() + " by " + player.getName().getString() + " " + player.getUuidAsString() + " in " + map.getDimension());
		}
	}
	
	private Map getMap() {
		if (getWorld().isClient)
			return null;
		if (!MarkerAPI.INSTANCE.isInitialised())
			return null;
		
		if (this.facing != Direction.UP)
			return null;
		
		OptionalInt mapId = getMapId();
		if (mapId.isEmpty())
			return null;
		
		MapState mapState = FilledMapItem.getMapState(mapId.getAsInt(), getWorld());
		if (mapState == null)
			return null;
		if (!mapState.locked)
			return null;
		ItemStack item = getHeldItemStack();
		if (!item.hasCustomName())
			return null;
		Text customName = item.getName();
		if (customName.getContent() instanceof TranslatableTextContent)
			return null;
		String name = customName.getString().strip().trim();
		if (name.isEmpty())
			return null;
		
		MapName mapName = MapName.Companion.fromName(name);
		if (mapName == null)
			return null;
		
		String dimension = getWorld().getRegistryKey().getValue().toString();
		
		return Map.Companion.fromMinecraftData(mapName.getName(), getBlockX(), getBlockY(), getBlockZ(), dimension, mapName.getAnchorX(), mapName.getAnchorY(), mapName.getTransparencyColor(), mapState.colors);
	}
}
