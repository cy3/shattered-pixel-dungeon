package com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Beam;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.YellowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.gun.SR.SR;

import java.text.DecimalFormat;
import java.util.ArrayList;

public abstract class Gun extends MeleeWeapon {
    public static final String AC_SHOOT		= "SHOOT";
    public static final String AC_RELOAD = "RELOAD";

    protected int max_round; //최대 장탄수
    protected int round; //현재 장탄수
    protected float reload_time = 2f; //재장전 시간
    protected int shotPerShoot = 1; //발사 당 탄환의 개수
    protected float shootingSpeed = 1f; //발사 시 소모하는 턴의 배율. 낮을수록 빠르다
    protected float shootingAccuracy = 1f; //발사 시 탄환 정확성의 배율. 높을 수록 정확하다
    protected boolean explode = false; //탄환 폭발 여부
    public static final String TXT_STATUS = "%d/%d";

    public boolean doubleBarrelSpecial = false;

    {
        defaultAction = AC_SHOOT;
        usesTargeting = true;

        hitSound = Assets.Sounds.HIT_CRUSH;
        hitSoundPitch = 0.8f;
    }

    private static final String ROUND = "round";
    private static final String MAX_ROUND = "max_round";
    private static final String RELOAD_TIME = "reload_time";
    private static final String SHOT_PER_SHOOT = "shotPerShoot";
    private static final String SHOOTING_SPEED = "shootingSpeed";
    private static final String SHOOTING_ACCURACY = "shootingAccuracy";
    private static final String EXPLODE = "explode";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(MAX_ROUND, max_round);
        bundle.put(ROUND, round);
        bundle.put(RELOAD_TIME, reload_time);
        bundle.put(SHOT_PER_SHOOT, shotPerShoot);
        bundle.put(SHOOTING_SPEED, shootingSpeed);
        bundle.put(SHOOTING_ACCURACY, shootingAccuracy);
        bundle.put(EXPLODE, explode);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        max_round = bundle.getInt(MAX_ROUND);
        round = bundle.getInt(ROUND);
        reload_time = bundle.getFloat(RELOAD_TIME);
        shotPerShoot = bundle.getInt(SHOT_PER_SHOOT);
        shootingSpeed = bundle.getFloat(SHOOTING_SPEED);
        shootingAccuracy = bundle.getFloat(SHOOTING_ACCURACY);
        explode = bundle.getBoolean(EXPLODE);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped( hero )) {
            actions.add(AC_SHOOT);
            actions.add(AC_RELOAD);
        }
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_SHOOT)) {
            if (!isEquipped( hero )) {
                usesTargeting = false;
                GLog.w(Messages.get(this, "not_equipped"));
            } else {
                if (round <= 0) { //현재 탄창이 0이면 AC_RELOAD 버튼을 눌렀을 때처럼 작동
                    execute(hero, AC_RELOAD);
                } else {
                    usesTargeting = true;
                    curUser = hero;
                    curItem = this;
                    GameScene.selectCell(shooter);
                }
            }
        }
        if (action.equals(AC_RELOAD)) {
            if (round == maxRound()){
                GLog.w(Messages.get(this, "already_loaded"));
            } else {
                reload();
            }
        }
    }

    @Override
    protected int baseChargeUse(Hero hero, Char target){
        return 2;
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        Dagger.sneakAbility(hero, 6, 4, this);
    }

    public void reload() {
        quickReload();
        hero.busy();
        hero.sprite.operate(hero.pos);

        onReload();
        Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
        hero.spendAndNext(reloadTime());
        GLog.i(Messages.get(this, "reload"));
    }

    public void onReload() {
    }

    public void quickReload() {
        round = maxRound();
        updateQuickslot();
    }

    public void manualReload() {
        manualReload(1, false);
    }

    public void manualReload(int amount, boolean overReload) {
        round += amount;
        if (overReload) {
            if (round > maxRound() + amount) { //최대 장탄수를 넘을 수는 있지만, 중첩은 불가
                round = maxRound() + amount;
            }
        } else {
            if (round > maxRound()) {
                round = maxRound();
            }
        }

        updateQuickslot();
    }

    public int shotPerShoot() { //발사 당 탄환의 수
        return shotPerShoot;
    }

    public int maxRound() { //최대 장탄수
        int amount = max_round;

        return amount;
    }

    public int round() {
        return round;
    }

    public float reloadTime() { //재장전에 소모하는 턴
        float amount = reload_time;
        return amount;
    }

    @Override
    public int max(int lvl) {
        return  3*(tier+1) +
                lvl*(tier+1); //근접 무기로서의 최대 데미지
    }

    public int bulletMin(int lvl) {
        return tier +
                lvl +
                RingOfSharpshooting.levelDamageBonus(hero);
    }
    public int bulletMin() {
        return bulletMin(this.buffedLvl());
    }

    //need to be overridden
    public abstract int bulletMax(int lvl);
    public int bulletMax() {
        return bulletMax(this.buffedLvl());
    }

    public int bulletDamage() {
        int damage = Random.NormalIntRange(bulletMin(), bulletMax());
        if (doubleBarrelSpecial) {
            damage = Math.round(damage * 0.667f);

        }

        damage = augment.damageFactor(damage);  //증강에 따라 변화하는 효과
        return damage;
    }

    @Override
    protected float baseDelay(Char owner) {
        if (doubleBarrelSpecial){
            return 0f;
        } else{
            return super.baseDelay(owner);
        }
    }

    @Override
    public String info() {
        String info = super.info();
        //근접 무기의 설명을 모두 가져옴, 여기에서 할 것은 근접 무기의 설명에 추가로 생기는 문장을 더하는 것
        if (levelKnown) { //감정되어 있을 때
            info += "\n\n" + Messages.get(Gun.class, "gun_desc",
                    augment.damageFactor(bulletMin(buffedLvl())), augment.damageFactor(bulletMax(buffedLvl())), shotPerShoot(), round, maxRound(), new DecimalFormat("#.##").format(reloadTime()));
        } else { //감정되어 있지 않을 때
            info += "\n\n" + Messages.get(Gun.class, "gun_typical_desc",
                    augment.damageFactor(bulletMin(0)), augment.damageFactor(bulletMax(0)), shotPerShoot(), round, maxRound(), new DecimalFormat("#.##").format(reloadTime()));
        }
        //DecimalFormat("#.##")은 .format()에 들어가는 매개변수(실수)를 "#.##"형식으로 표시하는데 사용된다.
        //가령 5.55555가 .format()안에 들어가서 .format(5.55555)라면, new DecimalFormat("#.##").format(5.55555)는 5.55라는 String 타입의 값을 반환한다.

        return info;
    }

    @Override
    public String status() { //아이템 칸 오른쪽 위에 나타내는 글자
        return Messages.format(TXT_STATUS, round, maxRound()); //TXT_STATUS 형식(%d/%d)으로, round, maxRound() 변수를 순서대로 %d부분에 출력
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        return knockBullet().targetingPos(user, dst);
    }

    //needs to be overridden
    public abstract Bullet knockBullet();

    public class Bullet extends MissileWeapon {

        public boolean isDoubleBarrel = false;

        {
            hitSound = Assets.Sounds.PUFF;
            tier = Gun.this.tier;
        }

        @Override
        public int proc(Char attacker, Char defender, int damage) {
            return Gun.this.proc(attacker, defender, damage);
        }

        @Override
        public int buffedLvl(){
            return Gun.this.buffedLvl();
        }

        @Override
        public int damageRoll(Char owner) {
            return bulletDamage();
        }

        @Override
        public boolean hasEnchant(Class<? extends Enchantment> type, Char owner) {
            return Gun.this.hasEnchant(type, owner);
        }

        @Override
        public float delayFactor(Char user) {
            return Gun.this.delayFactor(user) * shootingSpeed;
        }
        
        @Override
        public float accuracyFactor(Char owner, Char target) {
            float ACC = super.accuracyFactor(owner, target);
            if (owner instanceof Hero) {
                ACC *= shootingAccuracy;
            }
            return ACC;
        }

        @Override
        public int STRReq(int lvl) {
            return Gun.this.STRReq();
        }

        @Override
        protected void onThrow( int cell ) {
            if (explode) {
                ArrayList<Char> targets = new ArrayList<>();
                if (Actor.findChar(cell) != null) targets.add(Actor.findChar(cell));
                for (int i : PathFinder.NEIGHBOURS8){
                    if (Actor.findChar(cell + i) != null) targets.add(Actor.findChar(cell + i));
                }
                for (Char target : targets){
                    curUser.shoot(target, this);
                    if (target == hero && !target.isAlive()){
                        Dungeon.fail(getClass());
                        GLog.n(Messages.get(this, "ondeath"));
                    }
                }
                CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                ArrayList<Char> affected = new ArrayList<>();
                for (int n : PathFinder.NEIGHBOURS9) {
                    int c = cell + n;
                    if (c >= 0 && c < Dungeon.level.length()) {
                        if (Dungeon.level.heroFOV[c]) {
                            CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 4);
                        }
                        if (Dungeon.level.flamable[c]) {
                            Dungeon.level.destroy(c);
                            GameScene.updateMap(c);
                        }
                        Char ch = Actor.findChar(c);
                        if (ch != null) {
                            affected.add(ch);
                        }
                    }
                }
                Sample.INSTANCE.play( Assets.Sounds.BLAST );
            } else {
                Char enemy = Actor.findChar( cell );
                for (int i = 0; i < shotPerShoot(); i++) { //데미지 입히는 것과 발사 시 주변에서 나는 연기를 shotPerShoot만큼 반복
                    if (enemy == null || enemy == curUser) {
                        parent = null;
                        CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                        CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                    } else {
                        if (!curUser.shoot( enemy, this )) {
                            CellEmitter.get(cell).burst(SmokeParticle.FACTORY, 2);
                            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);
                        }
                    }
                }
            }

            // we consume ammo
            round --;
            
            // we alert nearby mobs
            for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) { 
                if (mob.paralysed <= 0
                        && Dungeon.level.distance(curUser.pos, mob.pos) <= 4
                        && mob.state != mob.HUNTING) {
                    mob.beckon( curUser.pos );
                }
            }
            updateQuickslot();
        }

        @Override
        public void throwSound() {
            Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 1, Random.Float(0.33f, 0.66f) );
        }

        @Override
        public void cast(final Hero user, final int dst) {
            cast(user, dst, false);
        }

        public void cast(final Hero user, final int dst, boolean doubleBarrel) {
            isDoubleBarrel = doubleBarrel;
            super.cast(user, dst);
        }
    }

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null) {
                if (target == curUser.pos) {
                    execute(hero, AC_RELOAD);
                } else {
                    //관통 사격
                    if (Gun.this instanceof SR) {

                        Ballistica beam = new Ballistica(curUser.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
                        ArrayList<Char> chars = new ArrayList<>();

                        for (int c : beam.subPath(1, beam.dist)) {
                            Char ch;

                            if ((ch = Actor.findChar( c )) != null) {
                                chars.add(ch);
                            }

                            CellEmitter.center( c ).burst( YellowParticle.BURST, Random.IntRange( 1, 2 ) );
                        }
                        int prevRound = round;
                        int damageBonus = 15;
                        int killed = 0;
                        for (Char ch : chars) {
                            Bullet bullet = knockBullet();
                            bullet.proc(curUser, ch, bullet.damageRoll(curUser) + damageBonus);
                            bullet.onThrow(ch.pos);
                            if (!ch.isAlive()) killed++;
                        }
                        if (chars.isEmpty()) {
                            Bullet bullet = knockBullet();
                            bullet.onThrow(beam.collisionPos);
                        }
                        round = prevRound - 1;
                        updateQuickslot();

                        Sample.INSTANCE.play( Assets.Sounds.HIT_CRUSH, 1, Random.Float(0.33f, 0.66f) );
                        CellEmitter.get(curUser.pos).burst(SmokeParticle.FACTORY, 2);
                        CellEmitter.center(curUser.pos).burst(BlastParticle.FACTORY, 2);

                        curUser.sprite.zap(target);
                        hero.spendAndNext(hero.attackDelay());

                        int cell = beam.path.get(Math.min(beam.dist, target));
                        curUser.sprite.parent.add(new Beam.ShotRay(curUser.sprite.center(), DungeonTilemap.raisedTileCenterToWorld( cell )));
                    } else {
                        knockBullet().cast(curUser, target);
                    }
                }
            }
        }
        @Override
        public String prompt() {
            return Messages.get(SpiritBow.class, "prompt");
        }
    };
}
