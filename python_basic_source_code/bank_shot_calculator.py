import pygame
import numpy as np
from PIL import Image
import math

# Define some colors
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
GRAY = (127, 127, 127)
GREEN = (0, 255, 0)
RED = (255, 0, 0)
BLUE = (50, 80, 220)
YELLOW = (255, 255, 0)


# initialize variables
pygame.init()

WIDTH = 1200
HEIGHT = 800
screen: pygame.Surface = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("title")
text = pygame.font.SysFont("Courier New", 16)
clock = pygame.time.Clock()

fps = 60
running = True  # must be true, else the program will terminate
block_map = None
plane_map = None
s1 = None
s2 = None
circles = []


class Plane:
    def __init__(self, x_y: int, bounds_min: int, bounds_max: int, vertical: bool, positive: bool):
        self.pos = x_y  # will be x if vertical and y if horizontal
        self.bounds_min = bounds_min
        self.bounds_max = bounds_max
        self.positive = positive  # will be True if facing north or east, False if south or west
        self.is_vertical = vertical
        self.highlighted = False

    # some factories
    @staticmethod
    def horizontal(y: int, pointing_down: bool, x_min: int, x_max: int):
        return Plane(y, x_min, x_max, False, pointing_down)

    @staticmethod
    def vertical(x: int, pointing_right: bool, y_min: int, y_max: int):
        return Plane(x, y_min, y_max, True, pointing_right)

    def render(self, scale, off_x, off_y):
        if self.is_vertical:
            x = self.pos * scale + off_x
            y1 = self.bounds_min * scale + off_y
            y2 = self.bounds_max * scale + off_y
            y3 = (y1 + y2) // 2
            pygame.draw.line(screen, RED, (x, y1), (x, y2), 5 if self.highlighted else 1)
            if self.positive:
                pygame.draw.line(screen, GREEN, (x + 1, y3), (x + scale // 4, y3))
            else:
                pygame.draw.line(screen, GREEN, (x - 1, y3), (x - scale // 4, y3))
        else:
            x1 = self.bounds_min * scale + off_x
            x2 = self.bounds_max * scale + off_x
            x3 = (x1 + x2) // 2
            y = self.pos * scale + off_y
            pygame.draw.line(screen, RED, (x1, y), (x2, y), 5 if self.highlighted else 1)
            if self.positive:
                pygame.draw.line(screen, GREEN, (x3, y + 1), (x3, y + scale // 4))
            else:
                pygame.draw.line(screen, GREEN, (x3, y - 1), (x3, y - scale // 4))
        self.highlighted = False

    def __str__(self):
        if self.is_vertical:
            return "V-Plane facing {: ^5} at x={} from {: ^2} -> {: ^2}".format(
                "right" if self.positive else "left", self.pos, self.bounds_min, self.bounds_max)
        else:
            return "H-Plane facing {: ^5} at y={} from {: ^2} -> {: ^2}".format(
                "up" if self.positive else "down", self.pos, self.bounds_min, self.bounds_max)

    def highlight(self):
        self.highlighted = True

    def create_mirror(self, plane):
        if plane.is_vertical:
            if self.is_vertical:
                new_pos = 2 * plane.pos - self.pos
                return Plane.vertical(new_pos, not self.positive, self.bounds_min, self.bounds_max)
            else:
                new_upper = 2 * plane.pos - self.bounds_min
                new_lower = 2 * plane.pos - self.bounds_max
                return Plane.horizontal(self.pos, self.positive, new_lower, new_upper)
        else:
            if self.is_vertical:
                new_upper = 2 * plane.pos - self.bounds_min
                new_lower = 2 * plane.pos - self.bounds_max
                return Plane.vertical(self.pos, self.positive, new_lower, new_upper)
            else:
                new_pos = 2 * plane.pos - self.pos
                return Plane.horizontal(new_pos, not self.positive, self.bounds_min, self.bounds_max)


class PlaneMap:
    def __init__(self, map_):
        self.spacing = map_.block_size
        self.off_x = map_.off_x
        self.off_y = map_.off_y
        self.planes = []
        for y in range(map_.grid.shape[0]):
            for x in range(map_.grid.shape[1]):
                if map_.grid[y][x] == 1:
                    self.planes.append(Plane.horizontal(y + 1, True, x, x + 1))
                    self.planes.append(Plane.horizontal(y, False, x, x + 1))
                    self.planes.append(Plane.vertical(x + 1, True, y, y + 1))
                    self.planes.append(Plane.vertical(x, False, y, y + 1))
        self.merge_planes()

    def get_horizontal_at(self, y: int):
        for plane in self.planes:
            if not plane.is_vertical and plane.pos == y:
                return plane
        return None

    def get_vertical_at(self, x: int):
        for plane in self.planes:
            if plane.is_vertical and plane.pos == x:
                return plane
        return None

    def merge_planes(self):
        """
        Takes many planes and eliminates planes which would be considered incorrect for the purposes of banking shots
        or collision. Replaces self.planes with a new list of sorted planes which are good for these things.
        """
        # vertical, horizontal. Essentially two of the same check but of two categories
        v_sorted = sorted([p for p in self.planes if p.is_vertical], key=lambda x: (x.pos, x.bounds_min))
        h_sorted = sorted([p for p in self.planes if not p.is_vertical], key=lambda x: (x.pos, x.bounds_min))

        # filter out directions which cancel
        # filter vertical oppositions
        mark_delete = []
        for i in range(len(v_sorted) - 1):
            this_v = v_sorted[i + 1]
            last_v = v_sorted[i]
            if this_v.pos == last_v.pos and this_v.bounds_min == last_v.bounds_min:
                mark_delete.append(i)  # the two planes are in the exact same position, must be opposing signs
        for i in range(len(mark_delete) - 1, -1, -1):  # delete all duplicate positions
            v_sorted.pop(mark_delete[i] + 1)
            v_sorted.pop(mark_delete[i])

        # filter horizontal oppositions
        mark_delete.clear()
        for i in range(len(h_sorted) - 1):
            this_h = h_sorted[i + 1]
            last_h = h_sorted[i]
            if this_h.pos == last_h.pos and this_h.bounds_min == last_h.bounds_min:
                mark_delete.append(i)  # the two planes are in the exact same position, must be opposing signs
        for i in range(len(mark_delete) - 1, -1, -1):  # delete all duplicate positions
            h_sorted.pop(mark_delete[i] + 1)
            h_sorted.pop(mark_delete[i])

        begin_merge = -1  # index of the starting merge block
        end_merge = -1  # index of the last merge block tested
        mark_merge = []
        for i in range(len(v_sorted)):
            this_v = v_sorted[i]
            last_v = v_sorted[end_merge]
            # these two planes must be on the same axis, have merging bounds and facing the same direction
            if begin_merge == -1 or this_v.pos != last_v.pos or \
                    this_v.bounds_min != last_v.bounds_max or \
                    this_v.positive != last_v.positive:
                # must check if the begin/end need to be merged
                if begin_merge != end_merge:
                    mark_merge.append((begin_merge, end_merge))
                # this plane becomes the new end of the merging
                begin_merge = i
            end_merge = i
        if begin_merge != end_merge:
            mark_merge.append((begin_merge, end_merge))
        for j in range(len(mark_merge) - 1, -1, -1):
            bi, ei = mark_merge[j]
            start = v_sorted[bi]
            pos = start.pos
            positive = start.positive
            p1 = start.bounds_min
            p2 = v_sorted[ei].bounds_max
            for i in range(ei, bi - 1, -1):  # eliminate planes which are being merged
                v_sorted.pop(i)
            v_sorted.insert(bi, Plane.vertical(pos, positive, p1, p2))  # create merged plane and insert into position

        begin_merge = -1  # index of the starting merge block
        end_merge = -1  # index of the last merge block tested
        mark_merge.clear()
        for i in range(len(h_sorted)):
            this_h = h_sorted[i]
            last_h = h_sorted[end_merge]
            # these two planes must be on the same axis, have merging bounds and facing the same direction
            if begin_merge == -1 or this_h.pos != last_h.pos or \
                    this_h.bounds_min != last_h.bounds_max or \
                    this_h.positive != last_h.positive:
                # must check if the begin/end need to be merged
                if begin_merge != end_merge:
                    mark_merge.append((begin_merge, end_merge))
                # this plane becomes the new end of the merging
                begin_merge = i
            end_merge = i
        if begin_merge != end_merge:
            mark_merge.append((begin_merge, end_merge))
        for j in range(len(mark_merge) - 1, -1, -1):
            bi, ei = mark_merge[j]
            start = h_sorted[bi]
            pos = start.pos
            positive = start.positive
            p1 = start.bounds_min
            p2 = h_sorted[ei].bounds_max
            for i in range(ei, bi - 1, -1):  # eliminate planes which are being merged
                h_sorted.pop(i)
            h_sorted.insert(bi, Plane.horizontal(pos, positive, p1, p2))  # create merged plane and insert into position
        # erase current planes and replace with these two clean lists of combined planes
        self.planes.clear()
        self.planes.extend(v_sorted)
        self.planes.extend(h_sorted)

    def render(self):
        for plane in self.planes:
            plane.render(self.spacing, self.off_x, self.off_y)

    def gridxview(self, x):
        return x * self.spacing + self.off_x

    def gridyview(self, y):
        return y * self.spacing + self.off_y

    def create_mirror(self, plane):  # in the future we'll want to precalculate this for each map
        """
        Flip this map over the given plane. This object is not modified.
        :param plane: axis to flip this planemap over
        :return: an exact copy of this planemap, except all planes are completely flipped over the given plane axis
        """
        mirrored = []
        for to_mirror in self.planes:
            mirrored.append(to_mirror.create_mirror(plane))
        return mirrored


class Map:
    def __init__(self, img_name, block_size, offset):
        img: Image.Image = Image.open(img_name).convert("RGB")  # We don't care about alpha
        self.grid = np.zeros((img.height, img.width), dtype=np.int)
        # planes are used for calculating possible bullet banking trajectories
        self.off_x = offset[0]
        self.off_y = offset[1]
        for y in range(img.height):
            for x in range(img.width):
                self.grid[y][x] = 0 if sum(img.getpixel((x, y))) > 381 else 1  # convert light -> 0, dark -> 1
        self.block_size = block_size
        # the calculation for a plane is any block creates a plane along all 4 of its edges
        # but each edge that is connected to another block is not a plane

    def render(self):
        by = 0
        bx = 0
        pygame.draw.rect(screen, WHITE, (self.off_x, self.off_y, self.grid.shape[1] * self.block_size,
                                         self.grid.shape[0] * self.block_size), 1)  # hollow outline of game area
        for y in range(self.grid.shape[0]):
            for x in range(self.grid.shape[1]):
                if self.grid[y][x] == 1:
                    pygame.draw.rect(screen, GRAY, (bx + self.off_x, by + self.off_y, self.block_size, self.block_size))
                bx += self.block_size
            bx = 0
            by += self.block_size


class Line:
    all = []

    def __init__(self, planemap, x1, y1, x2, y2, color, allowed):
        self.p1 = x1, y1
        self.p2 = x2, y2
        self.scaled = planemap.spacing
        self.offed_x = planemap.off_x
        self.offed_y = planemap.off_y
        self.color = color
        self.bad = True
        if x2 == x1:
            self.slope = None
            self.y_intersept = None
        else:
            self.slope = (y2 - y1) / (x2 - x1)  # this is a programmatical point-slope formula
            self.y_intersept = y1 - self.slope * x1
        if allowed:  # check if this line intersects with the allowed, but they must intersect
            banking_plane = allowed.pop(0)
            if not self.intersects(banking_plane):
                return
            for plane in planemap.planes:  # check if this line intersects any of the other planes, which is bad
                if plane is not banking_plane and self.intersects(plane, end=banking_plane):
                    plane.highlight()
                    return
            new_x, new_y = self.loose_intersect_position(banking_plane)
            self.p2 = new_x, new_y
            if banking_plane.is_vertical:
                new_angle = math.pi - math.atan2(y2 - new_y, x2 - new_x)
            else:
                new_angle = -math.atan2(y2 - new_y, x2 - new_x)
            # mirror the line to try the next angle
            new_mag = ((y2 - new_y) ** 2 + (x2 - new_x) ** 2) ** 0.5
            next_line = Line.create(planemap, new_x, new_y, new_angle, new_mag, self.color, allowed)
            if next_line.bad:
                return
            # for plane in allowed:
            #     if not self.intersects(plane):
            #         return
            #     # check if this line intersects any of the other mirror planes, which is bad
            #     for i in range(len(planemap.planes)):
            #         reg_plane = planemap.planes[i]
            #         mir_plane = reg_plane.create_mirror(plane)
            #         if reg_plane not in allowed and self.intersects(reg_plane, end=plane):
            #             plane.highlight()
            #             return
            #         if reg_plane not in allowed and self.intersects(mir_plane, start=plane):
            #             plane.highlight()
            #             return
        else:  # just check to make sure no planes at all intersect
            for plane in planemap.planes:  # check if this line intersects any of the other planes, which is bad
                if self.intersects(plane):
                    plane.highlight()
                    return
        Line.all.append(self)
        self.bad = False

    @staticmethod
    def create(planemap, x1, y1, angle, magnitude, color, allowed):
        return Line(planemap, x1, y1, x1 + math.cos(angle) * magnitude, y1 + math.sin(angle) * magnitude,
                    color, allowed)

    def render(self):
        if self.color is not None:
            pygame.draw.aaline(screen, self.color, self.p1, self.p2)

    def loose_intersect_position(self, plane):
        if plane.is_vertical:
            if self.slope is None:  # this shouldn't even happen
                return None
            planepos = plane.pos * self.scaled + self.offed_x
            intersect_y = self.slope * planepos + self.y_intersept  # plug in x
            return planepos, intersect_y
        else:
            if self.slope == 0:  # this also shouldn't happen
                return None
            planepos = plane.pos * self.scaled + self.offed_y
            if self.slope is None:  # can't do math with an OO slope
                intersect_x = self.p1[0]  # it literally has to intersect here
            else:
                intersect_x = (planepos - self.y_intersept) / self.slope  # solve for x
            return intersect_x, planepos

    def intersects(self, plane, start=None, end=None):
        if start:  # this clips the current line so planes behind the new_start won't collide
            pos = self.loose_intersect_position(start)
            if pos is None:
                return False
            else:
                p1 = pos
        else:
            p1 = self.p1
        if end:  # this clips the current line so planes behind the new_start won't collide
            pos = self.loose_intersect_position(end)
            if pos is None:
                return False
            else:
                p2 = pos
        else:
            p2 = self.p2
        if plane.is_vertical:
            if self.slope is None:  # mathematically, they can't intersect
                return False
            planepos = plane.pos * self.scaled + self.offed_x
            # otherwise, the segment doesn't even reach far enough
            if (p1[0] < planepos < p2[0]) or (p2[0] < planepos < p1[0]):
                intersect_y = self.slope * planepos + self.y_intersept  # plug in x
                if plane.bounds_min * self.scaled + self.offed_y < intersect_y < \
                        plane.bounds_max * self.scaled + self.offed_y:
                    return True
        else:
            if self.slope == 0:  # mathematically, they can't intersect
                return False
            planepos = plane.pos * self.scaled + self.offed_y
            # otherwise, the segment doesn't even reach far enough
            if (p1[1] < planepos < p2[1]) or (p2[1] < planepos < p1[1]):
                if self.slope is None:  # can't do math with an OO slope
                    intersect_x = p1[0]  # it literally has to intersect here
                else:
                    intersect_x = (planepos - self.y_intersept) / self.slope  # solve for x
                if plane.bounds_min * self.scaled + self.offed_x < intersect_x < \
                        plane.bounds_max * self.scaled + self.offed_x:
                    return True
        return False

    def __str__(self):
        return "Line: ({:.1f}, {:.1f}) -> ({:.1f}, {:.1f})".format(*self.p1, *self.p2)


class Shooter:
    def __init__(self, planemap, x, y):
        self.planemap = planemap
        self.block_scale = planemap.spacing
        self.size = 50
        quirk = (self.block_scale - self.size) / 2
        self.off_x = planemap.off_x + quirk
        self.off_y = planemap.off_y + quirk
        self.x = x
        self.y = y

    def render(self):
        pygame.draw.rect(screen, BLUE, (self.get_display_x(), self.get_display_y(), self.size, self.size))

    def get_display_x(self):
        return (self.x - 0.5) * self.block_scale + self.off_x

    def get_display_y(self):
        return (self.y - 0.5) * self.block_scale + self.off_y

    def get_center_x(self):
        return self.get_display_x() + self.size / 2

    def get_center_y(self):
        return self.get_display_y() + self.size / 2

    def shoot(self, x, y, allowed_intersections=None):
        angle = math.atan2(y - self.y, x - self.x)
        mag = self.planemap.spacing * ((y - self.y) ** 2 + (x - self.x) ** 2) ** 0.5
        shot = Line.create(self.planemap, self.get_center_x(), self.get_center_y(), angle, mag, None,
                           allowed_intersections)
        if shot.bad:
            return None
        else:
            return angle, mag

    @staticmethod
    def parse_results(*results):
        best_x = 0  # both are required to measure mean angle
        best_y = 0
        best_mag = 0
        total_num = 0
        for result in results:
            if result is not None:
                ang, mag = result
                best_x += math.cos(ang)
                best_y += math.sin(ang)
                best_mag += mag
                total_num += 1
        if total_num > 0:
            return math.atan2(best_y, best_x), best_mag / total_num
        else:
            return None

    def shoot_at(self, target, banks=0):
        """
        Attempts to shoot at the target using exactly <banks> amount of banks off walls
        :param target: other shooter to shoot at
        :param banks: exact number of banks off walls to use
        :return: number of discrete solutions to the shot. 0 means no shot was taken. > 0 means there was at least 1
        possibility and a random one was chosen
        """
        mid = (target.size / 2) / self.block_scale
        if banks == 0:
            shot0 = self.shoot(target.x, target.y)  # center
            shot1 = self.shoot(target.x - mid, target.y - mid)  # corner 1
            shot2 = self.shoot(target.x - mid, target.y + mid)  # corner 2
            shot3 = self.shoot(target.x + mid, target.y - mid)  # corner 3
            shot4 = self.shoot(target.x + mid, target.y + mid)  # corner 4
            result = Shooter.parse_results(shot0, shot1, shot2, shot3, shot4)
            if result is not None:
                Line.create(self.planemap, self.get_center_x(), self.get_center_y(), *result, WHITE, None)
            return result
        elif banks == 1:
            results = []
            good_planes = []
            for plane in self.planemap.planes:
                if plane.is_vertical:
                    # check directional optimization
                    if self.x < plane.pos and plane.positive or self.x > plane.pos and not plane.positive:
                        continue
                    # check special optimization for 1 bank
                    if self.x < plane.pos < target.x or self.x > plane.pos > target.x:
                        continue  # it's in the middle, there's no possible way we can use it
                    new_x = 2 * plane.pos - target.x
                    shot0 = self.shoot(new_x, target.y, [plane])
                    shot1 = self.shoot(new_x - mid, target.y - mid, [plane])
                    shot2 = self.shoot(new_x - mid, target.y + mid, [plane])
                    shot3 = self.shoot(new_x + mid, target.y - mid, [plane])
                    shot4 = self.shoot(new_x + mid, target.y + mid, [plane])
                    self.shoot_at(target, banks - 1)
                    result = Shooter.parse_results(shot0, shot1, shot2, shot3, shot4)
                    if result is not None:
                        results.append(result)
                        good_planes.append(plane)
                else:
                    # check directional optimization
                    if self.y < plane.pos and plane.positive or self.y > plane.pos and not plane.positive:
                        continue
                    # check special optimization for 1 bank
                    if self.y < plane.pos < target.y or self.y > plane.pos > target.y:
                        continue  # it's in the middle, there's no possible way we can use it
                    new_y = 2 * plane.pos - target.y
                    shot0 = self.shoot(target.x, new_y, [plane])
                    shot1 = self.shoot(target.x - mid, new_y - mid, [plane])
                    shot2 = self.shoot(target.x - mid, new_y + mid, [plane])
                    shot3 = self.shoot(target.x + mid, new_y - mid, [plane])
                    shot4 = self.shoot(target.x + mid, new_y + mid, [plane])
                    self.shoot_at(target, banks - 1)
                    result = Shooter.parse_results(shot0, shot1, shot2, shot3, shot4)
                    if result is not None:
                        results.append(result)
                        good_planes.append(plane)
            if len(results) == 0:
                return None
            shortest_mag = results[0][1]
            best_i = 0
            for i in range(1, len(results)):  # find shortest distance to shoot
                result = results[i]
                if result[1] < shortest_mag:
                    shortest_mag = result[1]
                    best_i = i
            Line.create(self.planemap, self.get_center_x(), self.get_center_y(), *results[best_i],
                        YELLOW, [good_planes[best_i]])
            return results[best_i]
        return None


def update():
    pressed = pygame.key.get_pressed()
    if pressed[pygame.K_w]:
        s2.y -= 0.02
    elif pressed[pygame.K_s]:
        s2.y += 0.02
    if pressed[pygame.K_a]:
        s2.x -= 0.02
    elif pressed[pygame.K_d]:
        s2.x += 0.02
    Line.all.clear()
    circles.clear()
    s1.shoot_at(s2, 1)
    s0.shoot_at(s2, 1)


def render():
    screen.fill(BLACK)

    block_map.render()
    plane_map.render()
    s1.render()
    s2.render()
    s0.render()
    for line in Line.all:
        line.render()
    for circle in circles:
        x, y = circle
        pygame.draw.circle(screen, WHITE, (int(x), int(y)), 5)

    pygame.display.flip()  # flip display buffers to display current rendering


def init():
    global block_map, plane_map, s1, s2, s0
    block_map = Map("map1.png", 75, (20, 20))
    plane_map = PlaneMap(block_map)
    s1 = Shooter(plane_map, 2, 2)
    s2 = Shooter(plane_map, 3, 6)
    s0 = Shooter(plane_map, 7, 8)


def main():
    global running
    while running:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_ESCAPE:
                    running = False

        update()
        render()
        clock.tick(fps)  # Limit to 60 frames per second


init()
main()
pygame.quit()
