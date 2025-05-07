import pandas as pd
import matplotlib.pyplot as plt
FILE = "galenModuleStatistics.csv"

# Plot two histograms in the same figure based on the number of concept names and role names
# Header of the csv file:
# Module name,Module size,Number of CN,Number of RN,Number of axioms

if __name__ == "__main__":
    # Read the csv file
    df = pd.read_csv(FILE)
    # keep only the columns of interest
    df = df[["Number of CN", "Number of RN"]]
    # Group by the number of CN and RN in different bins.
    # For RN: [0-10], [10-20], [20-30], [30-50], [50-100], [100-300], [300-max]
    # For CN: [0-10], [10-20], [20-30], [30-50], [50-100], [100-300], [300-1000], [1000-2000], [2000-5000], [5000-max]
    df["Number of CN"] = pd.cut(df["Number of CN"], bins=[0, 10, 20, 30, 50, 100, 300, 1000, 2000, 5000, df["Number of CN"].max()])
    df["Number of RN"] = pd.cut(df["Number of RN"], bins=[0, 10, 20, 30, 50, 100, 300, df["Number of RN"].max()])
    # Count the number of occurrences in each bin
    df = df.groupby(["Number of CN", "Number of RN"]).size().reset_index(name="count")
    # Plot the histograms
    plt.figure(figsize=(7, 4))
    plt.bar(df["Number of CN"].astype(str), df["count"], label="Modules with number of concept names in the range", alpha=0.5)
    plt.xlabel("Groups of modules with concept names in the range", fontsize=14)
    plt.ylabel("Occurrences", fontsize=14)
    # plt.title("Distribution of Number of CN")
    plt.legend()
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig("distribution_cn.png")
    plt.savefig("distribution_cn.pdf")
    plt.close()
    # Plot the histograms
    plt.figure(figsize=(7, 4))
    plt.bar(df["Number of RN"].astype(str), df["count"], label="Modules with number of role names in the range", alpha=0.5)
    plt.xlabel("Groups of modules with role names in the range", fontsize=14)
    plt.ylabel("Occurrences", fontsize=14)
    # plt.title("Distribution of Number of RN")
    plt.legend()
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig("distribution_rn.png")
    plt.savefig("distribution_rn.pdf")
    plt.close()