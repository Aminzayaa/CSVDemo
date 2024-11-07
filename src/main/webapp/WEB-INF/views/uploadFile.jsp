<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>高度なExcelデータ管理 (Advanced Excel Data Management)</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        /* Additional CSS for drag and drop styling */
        .drag-drop-area {
            border: 2px dashed #aaa;
            padding: 20px;
            text-align: center;
            color: #aaa;
            margin-top: 20px;
        }
        .drag-drop-area.highlight {
            border-color: #007bff;
            color: #007bff;
        }
    </style>
</head>
<body>
    <div class="container mt-4">
        <h4>高度なExcelデータ管理 (Advanced Excel Data Management)</h4>
        <div class="row">
            <!-- Left column: File Table -->
            <div class="col-md-8">
                <div class="mb-3">
                    <input type="text" class="form-control" placeholder="Search" />
                </div>
                <table class="table table-bordered upload-table">
                    <thead class="thead-light">
                        <tr>
                            <th>ファイル名 (File Name)</th>
                            <th>アップロード日時 (Upload Date)</th>
                            <th>アクション (Actions)</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                        List<String> tableNames = (List<String>) request.getAttribute("tableNames");
                        if (tableNames != null) {
                            for (String table : tableNames) {
                        %>
                        <tr>
                            <td><%= table %>.xlsx</td>
                            <td><%= new java.text.SimpleDateFormat("MM/dd/yyyy, hh:mm:ss a").format(new java.util.Date()) %></td>
                            <td>
                                <div class="action-buttons">
                                    <button type="button" title="Download" class="btn btn-outline-primary btn-sm">
                                        <i class="fa fa-download"></i>
                                    </button>
                                    <button type="button" title="Delete" class="btn btn-outline-danger btn-sm">
                                        <i class="fa fa-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <% 
                            }
                        } 
                        %>
                    </tbody>
                </table>
            </div>
            <!-- Right column: Drag and Drop Section -->
            <div class="col-md-4">
                <form action="upload" method="post" enctype="multipart/form-data" id="uploadForm">
                    <div class="drag-drop-area" id="drop-area">
                        <p>ファイルをここにドラッグ＆ドロップ (Drag and drop files here)</p>
                        <input type="file" id="fileInput" name="file" class="file-input-area" multiple />
                        <label for="fileInput" class="btn btn-outline-secondary">Choose File</label>
                    </div>
                    <button class="btn btn-primary mt-3" type="submit">アップロード (Upload)</button>
                    <% if (request.getAttribute("message") != null) { %>
                        <div>
                            <span class="<%= request.getAttribute("alertClass") %>"><%= request.getAttribute("message") %></span>
                        </div>
                    <% } %>
                </form>
            </div>
        </div>
    </div>
    
    <script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.bundle.min.js"></script>
    <script>
        // Handle drag and drop events
        const dropArea = document.getElementById('drop-area');
        const fileInput = document.getElementById('fileInput');
        const dropAreaText = dropArea.querySelector('p');

        // Handle dragover event to add a highlight effect
        dropArea.addEventListener('dragover', (event) => {
            event.preventDefault();
            dropArea.classList.add('highlight');
        });

        // Handle dragleave event to remove the highlight
        dropArea.addEventListener('dragleave', () => {
            dropArea.classList.remove('highlight');
        });

        // Handle drop event
        dropArea.addEventListener('drop', (event) => {
            event.preventDefault();
            dropArea.classList.remove('highlight');
            const files = event.dataTransfer.files;

            if (files.length > 0) {
                fileInput.files = files;
                dropAreaText.innerText = `Selected File: ${files[0].name}`;
            }
        });

        // Update the drop area text when a file is selected via the input
        fileInput.addEventListener('change', (event) => {
            const fileName = event.target.files.length > 0 ? event.target.files[0].name : '';
            dropAreaText.innerText = fileName 
                ? `Selected File: ${fileName}`
                : 'ファイルをここにドラッグ＆ドロップ (Drag and drop files here)';
        });
    </script>
</body>
</html>
