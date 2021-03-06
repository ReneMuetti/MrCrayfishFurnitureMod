/**
 * MrCrayfish's Furniture Mod
 * Copyright (C) 2014  MrCrayfish (http://www.mrcrayfish.com/)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mrcrayfish.furniture.blocks;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mrcrayfish.furniture.entity.EntitySittableBlock;
import com.mrcrayfish.furniture.init.FurnitureItems;
import com.mrcrayfish.furniture.tileentity.TileEntityCouch;
import com.mrcrayfish.furniture.util.CollisionHelper;
import com.mrcrayfish.furniture.util.SittableUtil;
import com.mrcrayfish.furniture.util.StateHelper;

public class BlockCouch extends BlockFurnitureTile
{
	public static final PropertyInteger COLOUR = PropertyInteger.create("colour", 0, 15);
	public static final PropertyEnum TYPE = PropertyEnum.create("type", CouchType.class);

	public BlockCouch(Material material)
	{
		super(material);
		setHardness(0.5F);
		setStepSound(Block.soundTypeCloth);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(TYPE, CouchType.BOTH).withProperty(COLOUR, Integer.valueOf(0)));
	}
	
	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
	{
		if (!(collidingEntity instanceof EntitySittableBlock))
		{
			setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
		}
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		int colour = ((TileEntityCouch) world.getTileEntity(pos)).getColour();
		state = state.withProperty(COLOUR, Integer.valueOf(colour));
		
		if (StateHelper.getBlock(world, pos, (EnumFacing) state.getValue(FACING), StateHelper.Direction.DOWN) instanceof BlockCouch)
		{
			if (StateHelper.getRotation(world, pos, (EnumFacing) state.getValue(FACING), StateHelper.Direction.DOWN) == StateHelper.Direction.RIGHT)
			{
				return state.withProperty(TYPE, CouchType.CORNER_RIGHT);
			}
			else if (StateHelper.getRotation(world, pos, (EnumFacing) state.getValue(FACING), StateHelper.Direction.DOWN) == StateHelper.Direction.LEFT)
			{
				return state.withProperty(TYPE, CouchType.CORNER_LEFT);
			}
		}
		
		boolean left = false;
		boolean right = false;
		
		if (!StateHelper.isAirBlock(world, pos, (EnumFacing) state.getValue(FACING), StateHelper.Direction.LEFT))
		{
			left = true;
		}
		if (!StateHelper.isAirBlock(world, pos, (EnumFacing) state.getValue(FACING), StateHelper.Direction.RIGHT))
		{
			right = true;
		}
		if(left && !right)
		{
			return state.withProperty(TYPE, CouchType.LEFT);
		}
		if(!left && right)
		{
			return state.withProperty(TYPE, CouchType.RIGHT);
		}
		if(!left && !right)
		{
			return state.withProperty(TYPE, CouchType.BOTH);
		}
		return state.withProperty(TYPE, CouchType.NONE);
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		IBlockState state = super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
		return state.withProperty(COLOUR, Integer.valueOf(0));
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack currentItem = playerIn.getCurrentEquippedItem();
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity instanceof TileEntityCouch)
		{
			TileEntityCouch tileEntityCouch = (TileEntityCouch) tileEntity;
			if (currentItem != null)
			{
				if (currentItem.getItem() instanceof ItemDye)
				{
					tileEntityCouch.setColour(currentItem.getItemDamage());
					currentItem.stackSize--;
					worldIn.markBlockForUpdate(pos);
					return true;
				}
			}
		}
		return SittableUtil.sitOnBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), playerIn, 0.45);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityCouch();
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return FurnitureItems.itemCouch;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos)
	{
		return new ItemStack(FurnitureItems.itemCouch);
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, new IProperty[] { FACING, COLOUR, TYPE });
	}

	public static enum CouchType implements IStringSerializable
	{
		NONE, LEFT, RIGHT, BOTH, CORNER_LEFT, CORNER_RIGHT;

		@Override
		public String getName()
		{
			return this.toString().toLowerCase();
		}
	}
}
