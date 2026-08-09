package functionhook.oldwu.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 镜子方块：贴墙放置（东西南北四个朝向）、无碰撞箱、可含水。
 *
 * <p>实现参考发光地衣/火把：通过 {@link HorizontalDirectionalBlock#FACING} 记录朝向，
 * 通过 {@link SimpleWaterloggedBlock} 支持含水；{@code noCollision} 使其没有碰撞箱，
 * 只保留用于高亮/交互的薄片选中形状。
 */
public class MirrorBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<MirrorBlock> CODEC = simpleCodec(MirrorBlock::new);

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	// 选中形状：贴合在墙上的 2 像素厚薄片（面板位于 FACING 的反方向，紧贴墙体）
	private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0);
	private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0);
	private static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 2.0, 16.0, 16.0);
	private static final VoxelShape SHAPE_WEST = Block.box(14.0, 0.0, 0.0, 16.0, 16.0, 16.0);

	protected MirrorBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = this.defaultBlockState();
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction[] directions = context.getNearestLookingDirections();
		for (Direction direction : directions) {
			if (direction.getAxis().isHorizontal()) {
				state = state.setValue(FACING, direction.getOpposite());
				if (state.canSurvive(level, pos)) {
					return state.setValue(WATERLOGGED, level.getFluidState(pos).isSourceOfType(Fluids.WATER));
				}
			}
		}
		return null;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos behind = pos.relative(facing.getOpposite());
		return level.getBlockState(behind).isFaceSturdy(level, behind, facing);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduler, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (state.getValue(WATERLOGGED)) {
			scheduler.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, level, scheduler, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case NORTH -> SHAPE_NORTH;
			case SOUTH -> SHAPE_SOUTH;
			case EAST -> SHAPE_EAST;
			default -> SHAPE_WEST;
		};
	}
}
