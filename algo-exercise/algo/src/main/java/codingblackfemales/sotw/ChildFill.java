package codingblackfemales.sotw;

public class ChildFill {
    private final long quantity;
    private final long price;
    private final int state;

    public ChildFill(long quantity, long price, int state) {
        this.quantity = quantity;
        this.price = price;
        this.state = state;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getPrice() {
        return price;
    }
}
