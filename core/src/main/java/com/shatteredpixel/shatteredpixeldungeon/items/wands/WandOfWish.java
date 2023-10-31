/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2023 Evan Debenham
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class WandOfWish extends Wand {

	{
		image = ItemSpriteSheet.WAND_WARDING;
	}

	@Override
	public int collisionProperties(int target) {
		if (cursed || !Dungeon.level.heroFOV[target])   return Ballistica.PROJECTILE;
		else                                            return Ballistica.STOP_TARGET;
	}

	private String wish = null;

	public static final String AC_WISH	= "WISH";
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add( AC_WISH );
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );
		if (action.equals( AC_WISH )) {
			GameScene.show(	new WndTextInput(
								Messages.get(this, "wish_title"),
								Messages.get(this, "wish_desc"),
								Messages.get(this, "wish_default"),
								20,
								false,
								Messages.get(this, "wish_set"),
								Messages.get(this, "wish_clear")){
							@Override
							public void onSelect(boolean positive, String text) {
								setWish(text);
							}
			});
		}
	}
	
	private void setWish(String text){
		if (text.equals("mob")) {
			wish = "mob";
		} else if (text.equals("upgrade")) {
			wish = "upgrade";
		} else if (text.equals("GDSM")) {
			wish = "GDSM";
		} else if (text.equals("str")) {
			wish = "str";
		} else {
			wish = null;
			GLog.w(Messages.get(this, "wish_set_fail"));
			return;
		}
		GLog.w(Messages.get(this, "wish_set_success", wish));
	}


	@Override
	public boolean tryToZap(Hero owner, int target) {
		if (wish == null) {
			GLog.w( Messages.get(this, "wish_not_set"));
			return false;
		} else {
			return super.tryToZap(owner, target);
		}
	}
	
	@Override
	public void onZap(Ballistica bolt) {

		int target = bolt.collisionPos;
		Char ch = Actor.findChar(target);

		if (!Dungeon.level.passable[target]){
			GLog.w( Messages.get(this, "bad_location"));
			Dungeon.level.pressCell(target);
			return;
		} else if (ch != null){
				GLog.w( Messages.get(this, "bad_location"));
				Dungeon.level.pressCell(target);
				return;
		} 
		GLog.w( Messages.get(this, "wish_granted"));
		if (wish.equals("mob")) {
			Mob mob = Dungeon.level.createMob();
			if (mob!=null) {
				mob.state = mob.WANDERING;
				mob.pos = target;
				GameScene.add(mob, 1f);
				ScrollOfTeleportation.appear(mob, mob.pos);
				Dungeon.level.occupyCell(mob);
				Dungeon.level.pressCell(target);
			}
		}
		if (wish.equals("upgrade")) {
			// give upgrade scroll
			ScrollOfUpgrade scroll =	Reflection.newInstance(ScrollOfUpgrade.class);
			Dungeon.level.drop(scroll, target).sprite.drop();
		}
		if (wish.equals("GDSM")) {
			// give GDSM
			PlateArmor armor =	Reflection.newInstance(PlateArmor.class);
			for (int i = 0; i < 10; i++) {
				armor.upgrade();
			}
			armor.inscribe();
			armor.identify();
			armor.cursed=false;
			Dungeon.level.drop(armor, target).sprite.drop();
		}
		if (wish.equals("str")) {
			// give potion of strength
			PotionOfStrength potion =	Reflection.newInstance(PotionOfStrength.class);
			Dungeon.level.drop(potion, target).sprite.drop();
		}
	}

	@Override
	public void fx(Ballistica bolt, Callback callback) {
		MagicMissile m = MagicMissile.boltFromChar(curUser.sprite.parent,
				MagicMissile.WARD,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		
		if (bolt.dist > 10){
			m.setSpeed(bolt.dist*20);
		}
		Sample.INSTANCE.play(Assets.Sounds.ZAP);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color( 0x88FFFF );
		particle.am = 0.3f;
		particle.setLifespan(3f);
		particle.speed.polar(Random.Float(PointF.PI2), 0.3f);
		particle.setSize( 1f, 2f);
		particle.radiateXY(2.5f);
	}

	@Override
	public String statsDesc() {
		if (levelKnown)
			return Messages.get(this, "stats_desc", level()+2);
		else
			return Messages.get(this, "stats_desc", 2);
	}

}
