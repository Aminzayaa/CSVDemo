<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>高度なExcelデータ管理 (Advanced Excel Data Management)</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>
    <div class="container">
        <h4>高度なExcelデータ管理 (Advanced Excel Data Management)</h4>
        <div class="row">
            <!-- Left column: File Table -->
            <div class="col-md-8">
                <!-- Search Bar -->
                <div class="mb-3">
                    <input type="text" class="form-control" placeholder="Search" />
                </div>
                <!-- Uploaded Files Table -->
                <table class="table table-bordered upload-table">
                    <thead class="thead-light">
                        <tr>
                            <th>ファイル名 (File Name)</th>
                            <th>アップロード日時 (Upload Date)</th>
                            <th>アクション (Actions)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>SB Works A.xlsx</td>
                            <td>10/9/2024, 10:50:24 AM</td>
                            <td>
                                <div class="action-buttons">
                                    <button type="button" title="Download" class="btn btn-outline-primary">
                                        <i class="fa fa-download"></i>
                                    </button>
                                    <button type="button" title="Delete" class="btn btn-outline-danger">
                                        <i class="fa fa-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <tr>
                            <td>SB Works B.xlsx</td>
                            <td>10/9/2024, 10:50:49 AM</td>
                            <td>
                                <div class="action-buttons">
                                    <button type="button" title="Download" class="btn btn-outline-primary">
                                        <i class="fa fa-download"></i>
                                    </button>
                                    <button type="button" title="Delete" class="btn btn-outline-danger">
                                        <i class="fa fa-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <!-- Right column: Drag and Drop Section -->
            <div class="col-md-4">
                <!-- File Upload Form -->
                <form action="upload" method="post" enctype="multipart/form-data" id="uploadForm">
                    <!-- Drag-and-Drop Area -->
                    <div class="drag-drop-area" id="drop-area">
                        <p>ファイルをここにドラッグ＆ドロップ (Drag and drop files here)</p>
                        <input type="file" id="fileInput" name="file" class="file-input-area" multiple />
                        <label for="fileInput" class="btn btn-outline-secondary">Choose File</label>
                    </div>
                    <button class="btn btn-primary mt-3" type="submit">アップロード (Upload)</button>
                    <c:if test="${not empty message}">
                        <div>
                            <span class="${alertClass}">${message}</span> <!-- No background color -->
                        </div>
                    </c:if>

                   
                </form>
            </div>
        </div>
    </div>
    <script>
        // Handle drag and drop events
        let dropArea = document.getElementById('drop-area');
        let fileInput = document.getElementById('fileInput');
        let uploadForm = document.getElementById('uploadForm');

        dropArea.addEventListener('dragover', (event) => {
            event.preventDefault();
            dropArea.classList.add('highlight');
        });

        dropArea.addEventListener('dragleave', () => {
            dropArea.classList.remove('highlight');
        });

        dropArea.addEventListener('drop', (event) => {
            event.preventDefault();
            dropArea.classList.remove('highlight');
            const files = event.dataTransfer.files;
            if (files.length > 0) {
                fileInput.files = files; // Assign dropped files to the file input
            }
        });

        // Optional: Handle file selection from the file input
        fileInput.addEventListener('change', (event) => {
            const fileName = event.target.files.length > 0 ? event.target.files[0].name : '';
            dropArea.querySelector('p').innerText = fileName || 'ファイルをここにドラッグ＆ドロップ (Drag and drop files here)';
        });
    </script>
</body>
</html>
