package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.AR;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class AR extends Gun {

    {
        max_round = 4;
        round = max_round;
    }

    public int maxRound() { //최대 장탄수
        int amount = super.maxRound();

        return amount;
    }

    @Override
    public int bulletMax(int lvl) {
        return 4 * (tier+1) +
                lvl * (tier+1) +
                RingOfSharpshooting.levelDamageBonus(hero);
    }

    @Override
    public int STRReq(int lvl) {
        int req = super.STRReq(lvl);
        return req;
    }

    @Override
    public float reloadTime() { //재장전에 소모하는 턴
        float amount = super.reloadTime();

        return amount;
    }

    @Override
    public Bullet knockBullet(){
        return new ARBullet();
    }

    public class ARBullet extends Bullet {
        {
            image = ItemSpriteSheet.SINGLE_BULLET;
        }
  }
    
}
