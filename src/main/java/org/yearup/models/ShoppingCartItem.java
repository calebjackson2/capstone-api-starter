package org.yearup.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

public class ShoppingCartItem
{
    private Product product;
    private int quantity = 1;
    private BigDecimal discountPercent = BigDecimal.ZERO;

    public Product getProduct()
    {
        return product;
    }

    public void setProduct(Product product)
    {
        this.product = product;
    }

    public int getQuantity()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountPercent()
    {
        return discountPercent;
    }

    public void setDiscountPercent(BigDecimal discountPercent)
    {
        this.discountPercent = discountPercent;
    }

    @JsonIgnore
    public int getProductId()
    {
        return product.getProductId();
    }

    public BigDecimal getLineTotal()
    {
        BigDecimal subTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(quantity));

        BigDecimal discountAmount = subTotal.multiply(discountPercent);
        return subTotal.subtract(discountAmount);
    }
}