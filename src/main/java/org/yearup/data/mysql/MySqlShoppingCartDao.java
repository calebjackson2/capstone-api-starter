package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    public MySqlShoppingCartDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public ShoppingCart getCartByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();

        String sql = """
        SELECT p.product_id,
               p.name,
               p.price,
               p.category_id,
               p.description,
               p.subcategory,
               p.stock,
               p.featured,
               p.image_url,
               sc.quantity
        FROM shopping_cart sc
        JOIN products p ON sc.product_id = p.product_id
        WHERE sc.user_id = ?
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();

            while (rs.next())
            {
                Product product = new Product(
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("category_id"),
                        rs.getString("description"),
                        rs.getString("subcategory"),
                        rs.getInt("stock"),
                        rs.getBoolean("featured"),
                        rs.getString("image_url")
                );

                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(rs.getInt("quantity"));

                // ✅ ADD TO MAP USING MODEL METHOD
                cart.add(item);
            }
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }

        return cart;
    }

    @Override
    public void addProductToCart(int userId, int productId, int quantity)
    {
        String sqlCheck = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        String sqlInsert = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        String sqlUpdate = "UPDATE shopping_cart SET quantity = quantity + ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection())
        {
            // check if the product is already in the cart
            PreparedStatement checkStmt = connection.prepareStatement(sqlCheck);
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();

            if(rs.next())
            {
                // product exists, update quantity
                PreparedStatement updateStmt = connection.prepareStatement(sqlUpdate);
                updateStmt.setInt(1, quantity);
                updateStmt.setInt(2, userId);
                updateStmt.setInt(3, productId);
                updateStmt.executeUpdate();
            }
            else
            {
                // product not in cart, insert new row
                PreparedStatement insertStmt = connection.prepareStatement(sqlInsert);
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, productId);
                insertStmt.setInt(3, quantity);
                insertStmt.executeUpdate();
            }
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity)
    {
        if (quantity <= 0)
        {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        String sql = "UPDATE shopping_cart SET quantity = ? WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);
            statement.executeUpdate();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    @Override
    public void clearCart(int userId)
    {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.executeUpdate();
        }
        catch(SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
}