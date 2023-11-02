package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.SG;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SG extends Gun {
    //근접 이외에 탄환이 맞지 않음. Hero.attackSkill()참고
    //탄환 기습 불가. Hero.canSurpriseAttack()참고

    {
        max_round = 1;
        reload_time = 1f;
        round = max_round;
        shotPerShoot = 5;
        shootingAccuracy = 3f;
    }

    @Override
    public void execute(Hero hero, String action) {
        if (action.equals(AC_RELOAD)) {
            reload();
            return;
        }
        super.execute(hero, action);
    }

    @Override
    public int bulletMax(int lvl) {
        return  (tier+1) +
                Math.round(0.5f * lvl * (tier+1)) + //2강 당 2/3/4/5/6 증가
                RingOfSharpshooting.levelDamageBonus(Dungeon.hero);
    }

    @Override
    public Bullet knockBullet(){
        return new SGBullet();
    }

    public class SGBullet extends Bullet {
        {
            image = ItemSpriteSheet.TRIPLE_BULLET;
        }
    }
}
