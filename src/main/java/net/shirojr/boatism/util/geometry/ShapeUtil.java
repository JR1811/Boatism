package net.shirojr.boatism.util.geometry;

import net.minecraft.util.math.MathHelper;

public class ShapeUtil {
    public record Position(int x, int y) {
        public Position add(Position input) {
            return new Position(this.x + input.x, this.y + input.y);
        }

        public Position getShiftedValue(Position input) {
            return new Position(input.x - this.x, input.y() - this.y);
        }
    }

    public static class Square {
        private Position squareStart, squareEnd;

        /**
         * Data type to make working with square hit registration easier
         *
         * @param posStart top left corner of square
         * @param posEnd   bottom right corner of square
         */
        public Square(Position posStart, Position posEnd) {
            this.squareStart = posStart;
            this.squareEnd = posEnd;
        }

        /**
         * Data type to make working with square hit registration easier
         *
         * @param squareStart top left corner of square
         * @param width       width of square
         * @param height      height of square
         */
        public Square(Position squareStart, int width, int height) {
            this.squareStart = squareStart;
            this.squareEnd = new Position(squareStart.x + width, squareStart.y + height);
        }

        public boolean isPositionInSquare(Position position) {
            return position.x >= this.getSquareStart().x && position.y >= this.getSquareStart().y &&
                    position.x <= this.getSquareEnd().x && position.y <= this.getSquareEnd().y;
        }

        public Position getSquareStart() {
            return squareStart;
        }

        public void setSquareStart(Position squareStart) {
            this.squareStart = squareStart;
        }

        public Position getSquareEnd() {
            return squareEnd;
        }

        public void setSquareEnd(Position squareEnd) {
            this.squareEnd = squareEnd;
        }

        public int getWidth() {
            return MathHelper.abs(getSquareEnd().x - getSquareStart().x);
        }

        public int getHeight() {
            return MathHelper.abs(getSquareEnd().y - getSquareStart().y);
        }

        public void moveSquare(Position newStartPosition) {
            Position movingDistance = this.getSquareStart().getShiftedValue(newStartPosition);
            this.setSquareStart(newStartPosition);
            this.setSquareEnd(this.getSquareEnd().add(movingDistance));
        }
    }
}
