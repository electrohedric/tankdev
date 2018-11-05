from pygame import *
from math import radians as rad, degrees as deg, sqrt, sin, cos, atan2
from numpy import zeros, int8
from collision import *


init()
size = width, height = 1280, 960


class Colors:
    BLACK = 0, 0, 0
    RED = 255, 0, 0
    GREEN = 0, 255, 0
    BLUE = 0, 0, 255
    WHITE = 255, 255, 255


class Font:
    SMALL = font.SysFont("cambriacambriamath", 20)
    LARGE = font.SysFont("cambriacambriamath", 50)


def load_image(name, ext=".png", alpha=True):
    img = image.load_extended("res/" + name + ext)
    if alpha:
        return img.convert_alpha()
    else:
        return img.convert()


screen = display.set_mode(size)  # type: Surface
clock = time.Clock()  # type: time.Clock
tankimg = load_image("tank")
turretimg = load_image("tank_gun")
floorimg = load_image("floor_tile")
wallimg = load_image("wall_tile")
basicbulletimg = load_image("basic_bullet")


class Game:
    event_queue = []
    tank_destroy_queue = []
    bullet_destroy_queue = []
    tank_create_queue = []
    bullet_create_queue = []
    tanks = {}
    bullets = {}
    player_index = 0
    player_lives = 3
    nexttank = 0
    nextbullet = 0
    currentcontrol = 0
    map = None
    target_fps = 120
    delta = 0

    @staticmethod
    def get_player():
        if Game.player_index in Game.tanks.keys():
            return Game.tanks[Game.player_index]
        else:
            return None

    @staticmethod
    def register_tank(tank):
        Game.tank_create_queue.append(tank)
        Game.nexttank += 1

    @staticmethod
    def register_bullet(bullet):
        Game.bullet_create_queue.append(bullet)
        Game.nextbullet += 1

    @staticmethod
    def destroy_tank(tank):
        Game.tank_destroy_queue.append(tank.id)

    @staticmethod
    def destroy_bullet(bullet):
        Game.bullet_destroy_queue.append(bullet.id)

    @staticmethod
    def update():
        for createtank in Game.tank_create_queue:  # tank creation
            Game.tanks[createtank.id] = createtank
        Game.tank_create_queue = []
        for createbullet in Game.bullet_create_queue:  # bullet creation
            Game.bullets[createbullet.id] = createbullet
        Game.bullet_create_queue = []

        for deltank in Game.tank_destroy_queue:  # tank destruction
            del Game.tanks[deltank]
        Game.tank_destroy_queue = []
        for delbullet in Game.bullet_destroy_queue:  # bullet destruction
            if delbullet in Game.bullets:
                del Game.bullets[delbullet]
            for tank in Game.tanks.values():
                tank.del_bullet(delbullet)
        Game.bullet_destroy_queue = []

        Game.test_collisions()

        for tank in Game.tanks.values():
            tank.update()
        for bullet in Game.bullets.values():
            bullet.update()

    @staticmethod
    def draw():
        Game.map.draw()
        for tank in Game.tanks.values():
            tank.draw()
        for bullet in Game.bullets.values():
            bullet.draw()

    @staticmethod
    def test_collisions():
        # collide all bullets
        for bullet in Game.bullets.values():
            for bullet2 in Game.bullets.values():  # with other bullets
                if bullet is bullet2:
                    continue
                if circle_collide_circle((bullet.x, bullet.y), bullet.radius, (bullet2.x, bullet2.y), bullet2.radius):
                    Game.destroy_bullet(bullet)
                    Game.destroy_bullet(bullet2)
                    # TODO play sound and animate
                    break

            for tank in Game.tanks.values():  # with other tanks, but not the source tank
                if tank != bullet.source and circle_collide_rect((bullet.x, bullet.y), bullet.radius, tank.vertices):
                    Game.destroy_bullet(bullet)
                    Game.destroy_tank(tank)
                    # TODO play sound and animate

        for tank in Game.tanks.values():
            for tank2 in Game.tanks.values():
                if tank is tank2 or tank.id in Game.tank_destroy_queue:
                    continue
                if rect_collide_rect(tank.vertices, tank2.vertices):
                    pass

    @staticmethod
    def load_map(filename):
        Game.tank_destroy_queue = []
        Game.bullet_destroy_queue = []
        Game.tank_create_queue = []
        Game.bullet_create_queue = []
        Game.tanks = {}
        Game.bullets = {}
        Game.nexttank = 0
        Game.nextbullet = 0
        Game.map = Map(filename)


class Map:
    AIR = 0
    WALL = 1
    HOLE = 2
    BRICK = 3
    AIRIMG = floorimg
    WALLIMG = wallimg
    HOLEIMG = floorimg
    BRICKIMG = wallimg

    def __init__(self, filename):
        with open("res/" + filename + ".txt") as f:
            mapinfo = f.read().replace('\n', '', 15)
        self.grid = zeros((15, 20), dtype=int8)
        for j in range(15):
            for i in range(20):
                data = mapinfo[i + j * 20]
                if data == 'X':
                    self.grid[j][i] = Map.WALL
                elif data == '.':
                    self.grid[j][i] = Map.HOLE
                elif data == 'O':
                    self.grid[j][i] = Map.BRICK
                elif data == 'p':
                    Game.register_tank(PlayerTank(i, j))
                elif data == 's':
                    Game.register_tank(StationaryTank(i, j))

    def draw(self):
        for y in range(15):
            for x in range(20):
                if self.grid[y][x] == Map.AIR:
                    screen.blit(self.AIRIMG, (x * 64, y * 64))
                elif self.grid[y][x] == Map.WALL:
                    screen.blit(self.WALLIMG, (x * 64, y * 64))


class Bullet:
    radius = 5
    speed = 230  # per second
    image = basicbulletimg
    tailanimation = None

    def __init__(self, source, x, y, angle):
        self.source = source
        self.id = Game.nextbullet
        self.x = x - self.radius
        self.y = y - self.radius
        self.image = transform.rotozoom(self.image, 0, 2 * self.radius / self.image.get_width())
        self.vx = self.speed * cos(angle)
        self.vy = self.speed * sin(angle)

    def update(self):
        self.x += self.vx * Game.delta
        self.y += self.vy * Game.delta
        if self.x < 0 or self.x > width or self.y < 0 or self.y > height:
            Game.destroy_bullet(self)

    def draw(self):
        screen.blit(self.image, (self.x, self.y))


class BasicBullet(Bullet):
    def __init__(self, source, x, y, angle):
        super().__init__(source, x, y, angle)


class Tank:
    bodyimg = tankimg  # Default tank
    gunimg = turretimg  # Default gun
    scale = 0.8  # Default full scale
    turn_speed = 500  # Default turn speed in deg/sec
    gunlen = 48  # Default gun img len
    max_projectiles = 2  # Default max number of in-air bullets
    max_speed = 120  # Default maximum allowed speed in pix/sec
    acceleration_rate = 12  # Default acceleration per frame
    brake_rate = 12  # Default braking speed per frame
    bullet = BasicBullet

    def __init__(self, x, y):
        self.id = Game.nexttank
        self.vx = 0
        self.vy = 0
        self.rot = 0
        self.w = self.h = self.bodyimg.get_width() * self.scale
        self.x = 64 * x + self.w / 2
        self.y = 64 * y + self.h / 2
        self.bodyimg = transform.rotozoom(self.bodyimg, 0, self.scale)
        self.gunimg = transform.rotozoom(self.gunimg, 0, self.scale)
        self.gunlen *= self.scale
        self.gunangle = 0
        self.vertices = None
        self.calculate_vertices()
        self.projectiles = []

    def draw(self):
        rotbody = transform.rotozoom(self.bodyimg, self.rot - 90, 1)  # type: Surface
        rotoff = rotbody.get_width() / 2.
        screen.blit(rotbody, (self.x - rotoff, self.y - rotoff))
        rotgun = transform.rotozoom(self.gunimg, -self.gunangle - 90, 1)
        rotoff = rotgun.get_width() / 2.
        screen.blit(rotgun, (self.x - rotoff, self.y - rotoff))

    def set_gunangle(self, angle):
        angle %= 360
        self.gunangle = angle

    def del_bullet(self, bulletid):
        if bulletid in self.projectiles:
            self.projectiles.remove(bulletid)

    def turn_toward(self, rot):
        deltarot = rot - self.rot
        if 0 < deltarot < 90 or deltarot < -90:
            if deltarot <= -90:
                deltarot += 180
            if deltarot < self.turn_speed * Game.delta:
                self.rot = rot
            else:
                self.rot += self.turn_speed * Game.delta
        else:
            if deltarot >= 90:
                deltarot -= 180
            if deltarot > self.turn_speed * Game.delta:
                self.rot = rot
            else:
                self.rot -= self.turn_speed * Game.delta

    def calculate_vertices(self):
        w2 = self.w / 2
        h2 = self.h / 2
        rx = self.x + w2
        lx = self.x - w2
        ty = self.y + h2
        by = self.y - h2
        rotrect = []
        for p in ((rx, ty), (rx, by), (lx, by), (lx, ty)):
            cx = p[0] - self.x
            cy = p[1] - self.y
            cosx = cos(rad(-self.rot))
            sinx = sin(rad(-self.rot))
            rotx = cx * cosx - cy * sinx
            roty = cx * sinx + cy * cosx
            nx = rotx + self.x
            ny = roty + self.y
            rotrect.append((nx, ny))
        self.vertices = rotrect

    def update(self):
        pass

    def shoot(self):
        if len(self.projectiles) < self.max_projectiles:
            gun = rad(self.gunangle)
            bullet = self.bullet(self, self.x + self.gunlen * cos(gun), self.y + self.gunlen * sin(gun), gun)
            self.projectiles.append(bullet.id)
            Game.register_bullet(bullet)


class StationaryTank(Tank):
    bodyimg = tankimg
    gunimg = turretimg
    max_projectiles = 2

    def __init__(self, x, y):
        super().__init__(x, y)

    def update(self):
        target = Game.get_player()
        if target is not None:
            self.set_gunangle(deg(atan2(target.y - self.y, target.x - self.x)))
            if time.get_ticks() % 300 == 0:
                self.shoot()


class PlayerTank(Tank):
    bodyimg = tankimg
    gunimg = turretimg
    max_projectiles = 5

    def __init__(self, x, y, active_control=True):
        super().__init__(x, y)
        Game.player_index = self.id
        if active_control:
            Game.currentcontrol = self.id

    def apply_brakes(self):
        if abs(self.vx) < self.brake_rate:
            self.vx = 0
        else:
            if self.vx > 0:
                self.vx -= self.brake_rate
            else:
                self.vx += self.brake_rate
        if abs(self.vy) < self.brake_rate:
            self.vy = 0
        else:
            if self.vy > 0:
                self.vy -= self.brake_rate
            elif self.vy < 0:
                self.vy += self.brake_rate
        
    def update(self):
        while Game.event_queue:
            nxt = Game.event_queue.pop()
            if nxt.type == MOUSEBUTTONDOWN:
                if nxt.button == 1:
                    self.shoot()
                    # TODO play sound
        allkeys = key.get_pressed()
        vdir = hdir = 0
        if allkeys[K_w]:
            vdir = 1
        elif allkeys[K_s]:
            vdir = -1
        if allkeys[K_a]:
            hdir = -1
        elif allkeys[K_d]:
            hdir = 1
        if vdir != 0 or hdir != 0:  # if you're applying gas
            hdir, vdir = norm((hdir, vdir))
            dirneeded = deg(atan2(vdir, hdir)) % 180
            self.rot %= 180
            if self.rot == dirneeded:
                self.vx += hdir * self.acceleration_rate
                self.vy -= vdir * self.acceleration_rate
                finalspeed = sqrt(self.vx * self.vx + self.vy * self.vy)
                if finalspeed > self.max_speed:
                    self.vx *= self.max_speed / finalspeed
                    self.vy *= self.max_speed / finalspeed
            else:
                if self.vx == 0 and self.vy == 0:
                    self.turn_toward(dirneeded)
                else:
                    self.apply_brakes()
        else:
            self.apply_brakes()
        if self.vx != 0 or self.vy != 0:
            self.x += self.vx * Game.delta
            self.y += self.vy * Game.delta
            self.calculate_vertices()
        mpos = mouse.get_pos()
        self.set_gunangle(deg(atan2(mpos[1] - self.y, mpos[0] - self.x)))


def game_loop():

    Game.load_map("level1")

    running = True
    while running:
        for e in event.get():
            if e.type == QUIT:
                running = False
            elif e.type == MOUSEBUTTONDOWN:
                Game.event_queue.append(e)

        # screen.fill(Colors.WHITE)

        Game.update()
        Game.draw()

        screen.blit(Font.SMALL.render(str(int(clock.get_fps())), 1, Colors.BLUE), (0, 0))
        display.flip()
        delta = clock.tick(Game.target_fps)
        Game.delta = delta / 1000.0
    quit()


game_loop()
