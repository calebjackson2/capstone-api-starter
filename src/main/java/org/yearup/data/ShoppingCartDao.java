package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getCartByUserId(int userId);
    void addProductToCart(int userId, int productId, int quantity);
    void updateProductQuantity (int userId, int productId, int quantity);
    void clearCart(int userId);
    // add additional method signatures here
}
