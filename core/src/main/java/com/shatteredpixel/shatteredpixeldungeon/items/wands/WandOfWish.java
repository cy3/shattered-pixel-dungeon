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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Shaman.EarthenBolt;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.PlateArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfInvisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.AR.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.GL.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.HG.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.MG.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.SG.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.SMG.*;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.SR.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Callback;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.Collections;

public class WandOfWish extends Wand {

	{
		image = ItemSpriteSheet.WAND_WARDING;
	}

	@Override
	public int collisionProperties(int target) {
		if (cursed || !Dungeon.level.heroFOV[target])   return Ballistica.PROJECTILE;
		else                                            return Ballistica.STOP_TARGET;
	}

	private Integer numUpgrade = 0;
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
								Messages.get(this, "wish_clear"),
								Messages.get(this, "wish_set")){
							@Override
							public void onSelect(boolean positive, String text) {
								setWish(text);
							}
			});
		}
	}

	private int parseWishInt(String text) {
		// read the level number
		String[] parts = text.split(" ");
		if (parts.length == 2) {
			try {
				int level = Integer.parseInt(parts[1]);
				return level;
			} catch (NumberFormatException e) {
				return -1;
			}
		} else {
			return -1;
		}
	}
	
	private void setWish(String text){
		numUpgrade = 0;
		if (text.startsWith("+")) {
			// consume first word 
			String[] parts = text.split(" ", 2);
			if (parts.length == 2) {
				// parse upgrade number
				try {
					numUpgrade = Integer.parseInt(parts[0].substring(1));
					text = parts[1];
				} catch (NumberFormatException e) {
					text = "";
				}
			} else {
				text = "";
			}
		}
		if (DeviceCompat.isDebug()) {
			// enable debug wish
		 	if (text.startsWith("level")) {
				// teleport to level
				Integer level = parseWishInt(text);
				if (level == -1) {
					GLog.w(Messages.get(this, "wish_set_fail"));
					return;
				}
				Level.beforeTransition();
				InterlevelScene.mode = InterlevelScene.Mode.RETURN;
				InterlevelScene.returnDepth = level;
				InterlevelScene.returnBranch = 0;
				InterlevelScene.returnPos = -1;
				Game.switchScene(InterlevelScene.class);
				return;
			}
			if (text.equals("fullstr")) {
				// full strength
				Hero hero = Dungeon.hero;
				PotionOfStrength pos = new PotionOfStrength();
				for (int i = 0; i < 10; i++) {
					pos.apply(hero);
				}
				return;
			}
			if (text.equals("fulllv")) {
				// full level
				Hero hero = Dungeon.hero;
				PotionOfExperience poe = new PotionOfExperience();
				for (int i = 0; i < 30; i++) {
					poe.apply(hero);
				}
				return;
			}
			if (text.equals("hp")) {
				// healing potion
				Hero hero = Dungeon.hero;
				PotionOfHealing poh = new PotionOfHealing();
					poh.apply(hero);
				return;
			}
			if (text.equals("light")) {
				// light buff
				Hero hero = Dungeon.hero;
				Buff.affect(hero, Light.class, Light.DURATION);
				return;
			}
			if (text.equals("killall")) {
				// kill all mobs
				for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
					if (mob != null) {
						mob.damage(mob.HP, new EarthenBolt());
					}
				}
				return;
			}
		}
		if (text.equals("mob")) {
			wish = "mob";
		} else if (text.equals("up")) {
			wish = "upgrade";
		} else if (text.equals("GDSM")) {
			wish = "GDSM";
		} else if (text.equals("AMPM")) {
			wish = "AMPM";
		} else if (text.equals("str")) {
			wish = "str";
		} else if (text.equals("disint")) {
			wish = "disintegration";
		} else if (text.equals("inv")) {
			wish = "invisibility";
		} else if (text.startsWith("AR") || text.startsWith("GL") || text.startsWith("HG") ||
				text.startsWith("MG") || text.startsWith("SG") || text.startsWith("SMG") || text.startsWith("SR")) {
			Integer level = parseWishInt(text);
			if (level == -1) {
				GLog.w(Messages.get(this, "wish_set_fail"));
				return;
			}
			wish = text;
		} else {
			wish = null;
			GLog.w(Messages.get(this, "wish_set_fail"));
			return;
		}
		GLog.w(Messages.get(this, "wish_set_success"));
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
			ArrayList<Integer> spawnPoints = new ArrayList<>();
			spawnPoints.add(target);
			if (numUpgrade > 1) {
				// (1^2+1~3^2): 1, (3^2+1~5^2):2, (5^2+1~7^2):3 ...
				Integer limit = (int) (Math.sqrt(numUpgrade - 1) + 1) / 2;
				PathFinder.buildDistanceMap( target, BArray.not( Dungeon.level.solid, null ), limit);
				for (int i = 0; i < PathFinder.distance.length; i++) {
					if (PathFinder.distance[i] < Integer.MAX_VALUE) {
						spawnPoints.add(i);
					}
				}
				// random select numUpgrade-1 points
				Collections.shuffle(spawnPoints);
				numUpgrade = Math.min(numUpgrade, spawnPoints.size());
				spawnPoints = new ArrayList<>(spawnPoints.subList(0, numUpgrade));
			}
			for (int i : spawnPoints){
				if (Dungeon.level.insideMap(i)
						&& Actor.findChar(i) == null
						&& !(Dungeon.level.pit[i])) {
					Mob mob = Dungeon.level.createMob();
					if (mob!=null) {
						mob.state = mob.WANDERING;
						mob.pos = i;
						GameScene.add(mob, 1f);
						ScrollOfTeleportation.appear(mob, mob.pos);
						Dungeon.level.occupyCell(mob);
						Dungeon.level.pressCell(target);
					}
				}
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
			for (int i = 0; i < numUpgrade; i++) {
				armor.upgrade();
			}
			armor.inscribe();
			armor.identify();
			armor.cursed=false;
			Dungeon.level.drop(armor, target).sprite.drop();
		}
		if (wish.equals("AMPM")) {
			// give AMPM
			PlateArmor armor =	Reflection.newInstance(PlateArmor.class);
			for (int i = 0; i < numUpgrade; i++) {
				armor.upgrade();
			}
			Glyph gl = (Glyph) Reflection.newInstance(AntiMagic.class);
			armor.inscribe(gl);
			armor.identify();
			armor.cursed=false;
			Dungeon.level.drop(armor, target).sprite.drop();
		}
		if (wish.equals("str")) {
			// give potion of strength
			PotionOfStrength potion =	Reflection.newInstance(PotionOfStrength.class);
			potion.identify();
			Dungeon.level.drop(potion, target).sprite.drop();
		}
		if (wish.equals("invisibility")) {
			// give potion of invisibility
			PotionOfInvisibility potion =	Reflection.newInstance(PotionOfInvisibility.class);
			potion.identify();
			Dungeon.level.drop(potion, target).sprite.drop();
		}
		if (wish.equals("disintegration")) {
			// give wand of disintegration
			WandOfDisintegration wand =	Reflection.newInstance(WandOfDisintegration.class);
			for (int i = 0; i < numUpgrade; i++) {
				wand.upgrade();
			}
			wand.identify();
			Dungeon.level.drop(wand, target).sprite.drop();
		}
		if (wish.startsWith("AR") || wish.startsWith("GL") || wish.startsWith("HG") ||
				wish.startsWith("MG") || wish.startsWith("SG") || wish.startsWith("SMG") || wish.startsWith("SR")) {
			Integer level = parseWishInt(wish);
			Gun gun = null;
			if (wish.startsWith("AR")) {
				if (level == 1) {
					gun = Reflection.newInstance(AR_tier1.class);
				} else if (level ==2) {
					gun = Reflection.newInstance(AR_tier2.class);
				} else if (level ==3) {
					gun = Reflection.newInstance(AR_tier3.class);
				} else if (level ==4) {
					gun = Reflection.newInstance(AR_tier4.class);
				} else if (level ==5) {
					gun = Reflection.newInstance(AR_tier5.class);
				}
			}
			if (wish.startsWith("GL")) {
				if (level == 1) {
					gun = Reflection.newInstance(GL_tier1.class);
				} else if (level ==2) {
					gun = Reflection.newInstance(GL_tier2.class);
				} else if (level ==3) {
					gun = Reflection.newInstance(GL_tier3.class);
				} else if (level ==4) {
					gun = Reflection.newInstance(GL_tier4.class);
				} else if (level ==5) {
					gun = Reflection.newInstance(GL_tier5.class);
				}
			}
			if (wish.startsWith("HG")) {
				if (level == 1) {
					gun = Reflection.newInstance(HG_tier1.class);
				} else if (level ==2) {
					gun = Reflection.newInstance(HG_tier2.class);
				} else if (level ==3) {
					gun = Reflection.newInstance(HG_tier3.class);
				} else if (level ==4) {
					gun = Reflection.newInstance(HG_tier4.class);
				} else if (level ==5) {
					gun = Reflection.newInstance(HG_tier5.class);
				}
			}
			if (wish.startsWith("MG")) {
				if (level == 1) {
					gun = Reflection.newInstance(MG_tier1.class);
				} else if (level ==2) {
					gun = Reflection.newInstance(MG_tier2.class);
				} else if (level ==3) {
					gun = Reflection.newInstance(MG_tier3.class);
				} else if (level ==4) {
					gun = Reflection.newInstance(MG_tier4.class);
				} else if (level ==5) {
					gun = Reflection.newInstance(MG_tier5.class);
				}
			}
			if (wish.startsWith("SG")) {
				if (level == 1) {
					gun = Reflection.newInstance(SG_tier1.class);
				} else if (level ==2) {
					gun = Reflection.newInstance(SG_tier2.class);
				} else if (level ==3) {
					gun = Reflection.newInstance(SG_tier3.class);
				} else if (level ==4) {
					gun = Reflection.newInstance(SG_tier4.class);
				} else if (level ==5) {
					gun = Reflection.newInstance(SG_tier5.class);
				}
			}
			if (wish.startsWith("SMG")) {
				if (level == 1) {
					gun = Reflection.newInstance(SMG_tier1.class);
				} else if (level ==2) {
					gun = Reflection.newInstance(SMG_tier2.class);
				} else if (level ==3) {
					gun = Reflection.newInstance(SMG_tier3.class);
				} else if (level ==4) {
					gun = Reflection.newInstance(SMG_tier4.class);
				} else if (level ==5) {
					gun = Reflection.newInstance(SMG_tier5.class);
				}
			}
			if (wish.startsWith("SR")) {
				if (level == 1) {
					gun = Reflection.newInstance(SR_tier1.class);
				} else if (level ==2) {
					gun = Reflection.newInstance(SR_tier2.class);
				} else if (level ==3) {
					gun = Reflection.newInstance(SR_tier3.class);
				} else if (level ==4) {
					gun = Reflection.newInstance(SR_tier4.class);
				} else if (level ==5) {
					gun = Reflection.newInstance(SR_tier5.class);
				}
			}
			if (gun != null) {
				gun.identify();
				for (int i = 0; i < numUpgrade; i++) {
					gun.upgrade();
				}
				Dungeon.level.drop(gun, target).sprite.drop();
			}
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
