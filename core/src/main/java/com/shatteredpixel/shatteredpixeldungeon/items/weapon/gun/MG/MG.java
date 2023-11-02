package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.MG;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
public class MG extends Gun {

    {
        max_round = 4;
        round = max_round;
        reload_time = 3f;
        shotPerShoot = 3;
        shootingAccuracy = 0.7f;
    }


    @Override
    public int bulletMin(int lvl) {
        return tier +
                lvl/2 +
                RingOfSharpshooting.levelDamageBonus(hero);
    }

    @Override
    public int bulletMax(int lvl) {
        return 3 * (tier+1) +
                lvl * (tier+1) +
                RingOfSharpshooting.levelDamageBonus(hero);
    }

    @Override
    public Bullet knockBullet(){
        return new MGBullet();
    }

    public class MGBullet extends Bullet {
        {
            image = ItemSpriteSheet.TRIPLE_BULLET;
        }
    }
}
