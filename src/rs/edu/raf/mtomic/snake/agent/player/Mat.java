package rs.edu.raf.mtomic.snake.agent.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Mat implements Cloneable{

    public final int rows;
    public final int cols;

    public final double[][] data;

    public Mat(int rows, int cols)
    {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public Mat(double[][] data)
    {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = new double[rows][cols];
        // Simple copy of a 2-dimensional double array
        for(int r = 0; r < rows; r++)
        {
            for(int c = 0; c < cols; c++)
            {
                this.data[r][c] = data[r][c];
            }
        }
    }

    public Mat(int rows, int cols, ArrayList<ArrayList<Integer>> weights)
    {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
        // Simple copy of a 2-dimensional double array

        for(int r = 0; r < rows; r++)
        {
            for(int c = 0; c < cols; c++)
            {
                this.data[r][c] = weights.get(r).get(c);
            }
        }
    }

    public Mat(int rows, int cols, int i, List<List<Double>> weights)
    {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
        // Simple copy of a 2-dimensional double array
        int count = 0;

        for(int r = 0; r < rows; r++)
        {
            for(int c = 0; c < cols; c++)
            {
                this.data[r][c] = weights.get(i).get(count);
                count++;
            }
        }
    }

    public Mat add(final Mat mat)
    {
        return new Mat(data).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return val + mat.data[r][c];
            }
        });
    }

    public Mat add(final double v)
    {
        return new Mat(data).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return val + v;
            }
        });
    }

    public Mat subtract(final Mat mat)
    {
        return new Mat(data).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return val - mat.data[r][c];
            }
        });
    }

    public Mat subtract(final double v)
    {
        return new Mat(data).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return val - v;
            }
        });
    }

    public Mat transpose()
    {
        return new Mat(cols, rows).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return data[c][r];
            }
        });
    }

    public Mat mult(final double scl)
    {
        return new Mat(data).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return val * scl;
            }
        });
    }

    public Mat elementMult(final Mat mat)
    {
        return new Mat(data).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return val * mat.data[r][c];
            }
        });
    }

    public Mat mult(final Mat mat)
    {
        if(cols != mat.rows) throw new RuntimeException("Rows don't match columns");

        return new Mat(rows, mat.cols).map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                double sum = 0;
                for(int i = 0; i < cols; i++)
                {
                    sum += data[r][i] * mat.data[i][c];
                }
                return sum;
            }
        });
    }

    public Mat map(MatFunc func)
    {
        for(int r = 0; r < rows; r++)
        {
            for(int c = 0; c < cols; c++)
            {
                data[r][c] = func.perform(data[r][c], r, c);
            }
        }
        return this;
    }

    public double[] toArray()
    {
        double[] arr = new double[rows * cols];
        for(int r = 0; r < rows; r++)
        {
            for(int c = 0; c < cols; c++)
            {
                arr[c + r * cols] = data[r][c];
            }
        }
        return arr;
    }

    public double[] getColumn(int col)
    {
        double[] column = new double[rows];
        for(int i = 0; i < rows; i++)
        {
            column[i] = data[i][col];
        }
        return column;
    }

    public Mat clone()
    {
        return new Mat(data);
    }

    public String toArrayString()
    {
        return Arrays.deepToString(data);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for(int r = 0; r < rows; r++)
        {
            sb.append("[");
            for(int c = 0; c < cols; c++)
            {
                sb.append(data[r][c]);

                if(c < cols - 1) sb.append(", ");
            }

            sb.append(']');
            if(r < rows - 1) sb.append('\n');
        }
        return sb.toString();
    }

    public static Mat fromArray(double[] arr)
    {
        Mat mat = new Mat(arr.length, 1);
        for(int i = 0; i < arr.length; i++)
        {
            mat.data[i][0] = arr[i];
        }
        return mat;
    }

    public interface MatFunc
    {
        /**
         * This method takes the value at the current matrix row
         * and index and performs some operation on it. Then returns
         * what value should now be at that index.
         *
         * @param val The value at the the current row and column
         * @param r The current row
         * @param c The current column
         *
         * @return This should return the new value at the position
         */
        public double perform(double val, int r, int c);
    }

    public static MatFunc ReLu = new MatFunc()
    {
        @Override
        public double perform(double val, int r, int c)
        {
            return Math.max(0, val);
        }
    };

    public static MatFunc SIGMOID = new MatFunc()
    {
        @Override
        public double perform(double val, int r, int c)
        {
            return 1 / (1 + Math.exp(-val));
        }
    };

    public static MatFunc SIGMOID_DERIVATIVE = new MatFunc()
    {
        @Override
        public double perform(double val, int r, int c)
        {
            return val * (1 - val);
        }
    };

    public static MatFunc TANH = new MatFunc()
    {
        @Override
        public double perform(double val, int r, int c)
        {
            return Math.tanh(val);
        }
    };

    public static MatFunc TANH_DERIVATIVE = new MatFunc()
    {
        @Override
        public double perform(double val, int r, int c)
        {
            return 1 - val * val;
        }
    };

    public Mat randomize(final Random rand)
    {
        return map(new MatFunc()
        {
            @Override
            public double perform(double val, int r, int c)
            {
                return rand.nextDouble() * 2 - 1;
            }
        });
    }

    public Mat randomize()
    {
        return randomize(ThreadLocalRandom.current());
    }
}
