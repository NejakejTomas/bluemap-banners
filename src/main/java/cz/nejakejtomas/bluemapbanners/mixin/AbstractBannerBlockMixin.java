package cz.nejakejtomas.bluemapbanners.mixin;

import cz.nejakejtomas.bluemapbanners.MarkerAPI;
import cz.nejakejtomas.bluemapbanners.markers.banners.Banner;
import cz.nejakejtomas.bluemapbanners.markers.banners.Pattern;
import cz.nejakejtomas.bluemapbanners.markers.banners.PatternType;
import cz.nejakejtomas.bluemapbanners.utils.MinecraftColor;
import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(AbstractBannerBlock.class)
public abstract class AbstractBannerBlockMixin extends BlockWithEntity
{
	private final Logger LOGGER = LoggerFactory.getLogger(AbstractBannerBlockMixin.class);
	
	protected AbstractBannerBlockMixin(Settings settings) {
		super(settings);
	}
	
	@Inject(at = @At("TAIL"), method = "onPlaced(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;)V", require = 1)
	private void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack, CallbackInfo callbackInfo) {
		if (!(placer instanceof final PlayerEntity player))
			return;
		
		if (world.isClient)
			return;
		if (!MarkerAPI.INSTANCE.isInitialised())
			return;
		Optional<BannerBlockEntity> bannerEntity = world.getBlockEntity(pos, BlockEntityType.BANNER);
		if (bannerEntity.isEmpty())
			return;
		BannerBlockEntity blockEntity = bannerEntity.get();
		Text customName = blockEntity.getCustomName();
		if (customName == null)
			return;
		if (customName.getContent() instanceof TranslatableTextContent)
			return;
		
		String name = getBannerName(customName);
		if (name == null)
			return;
		
		String dimension = player.getWorld().getRegistryKey().getValue().toString();
		
		MinecraftColor color = colorFromDyeColor(bannerEntity.get().getColorForState());
		List<Pattern> patterns = blockEntity.getPatterns().stream().map(it -> {
			PatternType type = PatternType.Companion.getById().get(it.getFirst().value().getId());
			MinecraftColor mColor = colorFromDyeColor(it.getSecond());
			return new Pattern(type, mColor);
		}).filter(pattern -> pattern.getType() != PatternType.Base).toList();
		
		Banner banner = new Banner(name, pos.getX(), pos.getY(), pos.getZ(), dimension, color, patterns);
		
		if (MarkerAPI.INSTANCE.addMarker(banner)) {
			LOGGER.info("Banner marker created " + banner.getPositionX() + " " + banner.getPositionY() + " " + banner.getPositionZ() + " by " + player.getName().getString() + " " + placer.getUuidAsString() + " in " + banner.getDimension());
		}
	}
	
	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		Optional<BannerBlockEntity> bannerEntity = world.getBlockEntity(pos, BlockEntityType.BANNER);
		super.onBreak(world, pos, state, player);
		
		if (world.isClient)
			return;
		if (!MarkerAPI.INSTANCE.isInitialised())
			return;
		if (bannerEntity.isEmpty())
			return;
		BannerBlockEntity blockEntity = bannerEntity.get();
		Text customName = blockEntity.getCustomName();
		if (customName == null)
			return;
		if (customName.getContent() instanceof TranslatableTextContent)
			return;
		
		String name = getBannerName(customName);
		if (name == null)
			return;
		
		String dimension = player.getWorld().getRegistryKey().getValue().toString();
		
		MinecraftColor color = colorFromDyeColor(bannerEntity.get().getColorForState());
		List<Pattern> patterns = blockEntity.getPatterns().stream().map(it -> {
			PatternType type = PatternType.Companion.getById().get(it.getFirst().value().getId());
			MinecraftColor mColor = colorFromDyeColor(it.getSecond());
			return new Pattern(type, mColor);
		}).filter(pattern -> pattern.getType() != PatternType.Base).toList();
		
		Banner banner = new Banner(name, pos.getX(), pos.getY(), pos.getZ(), dimension, color, patterns);
		
		if (MarkerAPI.INSTANCE.removeMarker(banner)) {
			LOGGER.info("Banner marker removed " + banner.getPositionX() + " " + banner.getPositionY() + " " + banner.getPositionZ() + " by " + player.getName().getString() + " " + player.getUuidAsString() + " in " + banner.getDimension());
		}
	}
	
	private String getBannerName(Text text) {
		String name = text.getString().strip().trim();
		if (name.isEmpty())
			return null;
		
		return name;
	}
	
	private MinecraftColor colorFromDyeColor(DyeColor dyeColor) {
		return switch (dyeColor) {
			case WHITE -> MinecraftColor.White;
			case ORANGE -> MinecraftColor.Orange;
			case MAGENTA -> MinecraftColor.Magenta;
			case LIGHT_BLUE -> MinecraftColor.LightBlue;
			case YELLOW -> MinecraftColor.Yellow;
			case LIME -> MinecraftColor.Lime;
			case PINK -> MinecraftColor.Pink;
			case GRAY -> MinecraftColor.Gray;
			case LIGHT_GRAY -> MinecraftColor.LightGray;
			case CYAN -> MinecraftColor.Cyan;
			case PURPLE -> MinecraftColor.Purple;
			case BLUE -> MinecraftColor.Blue;
			case BROWN -> MinecraftColor.Brown;
			case GREEN -> MinecraftColor.Green;
			case RED -> MinecraftColor.Red;
			case BLACK -> MinecraftColor.Black;
		};
	}
}