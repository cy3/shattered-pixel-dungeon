package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.SMG;
import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
public class SMG extends Gun {

    {
        max_round = 2;
        round = max_round;
        shotPerShoot = 3;
        shootingAccuracy = 1.2f;
    }

    @Override
    public int bulletMin(int lvl) {
        return tier +
                lvl +
                RingOfSharpshooting.levelDamageBonus(hero);
    }

    @Override
    public int bulletMax(int lvl) {
        return 3 * (tier) +
                lvl * (tier) +
                RingOfSharpshooting.levelDamageBonus(hero);
    }

    @Override
    public Bullet knockBullet(){
        return new SMGBullet();
    }

    public class SMGBullet extends Bullet {
        {
            image = ItemSpriteSheet.TRIPLE_BULLET;
        }

    }
}
