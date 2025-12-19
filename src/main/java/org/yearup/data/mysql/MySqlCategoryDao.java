package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao
{
    public MySqlCategoryDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Category> getAllCategories()
    {
        List<Category> categories = new ArrayList<>();
        String sql = """
            SELECT category_id, name, description
            FROM categories
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery())
        {
            while (results.next())
            {
                categories.add(mapRow(results));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return categories;
    }

    @Override
    public Category getById(int categoryId)
    {
        String sql = """
            SELECT category_id, name, description
            FROM categories
            WHERE category_id = ?
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);

            try (ResultSet results = statement.executeQuery())
            {
                if (results.next())
                {
                    return mapRow(results);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public Category create(Category category)
    {
        String sql = """
            INSERT INTO categories (name, description)
            VALUES (?, ?)
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys())
            {
                if (keys.next())
                {
                    category.setCategoryId(keys.getInt(1));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return category;
    }

    @Override
    public void update(int categoryId, Category category)
    {
        String sql = """
            UPDATE categories
            SET name = ?, description = ?
            WHERE category_id = ?
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            statement.setInt(3, categoryId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void delete(int categoryId)
    {
        String sql = """
            DELETE FROM categories
            WHERE category_id = ?
        """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, categoryId);
            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Category mapRow(ResultSet row) throws SQLException
    {
        int categoryId = row.getInt("category_id");
        String name = row.getString("name");
        String description = row.getString("description");

        Category category = new Category()
        {{
            setCategoryId(categoryId);
            setName(name);
            setDescription(description);
        }};

        return category;
    }

}
